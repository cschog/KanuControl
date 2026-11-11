package com.kcserver.vermietung.service;

import com.kcserver.vermietung.dto.VerbrauchskostenVorschauDTO;
import com.kcserver.vermietung.dto.AblesungBestaetigungDTO;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsquelle;
import com.kcserver.core.exception.ErrorMessages;

import com.kcserver.vermietung.repository.BuchungVerbrauchskostenRepository;
import com.kcserver.vermietung.repository.VerbrauchskostenRepository;
import com.kcserver.vermietung.repository.ZaehlerRepository;
import com.kcserver.vermietung.repository.ZaehlerstandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

import java.time.LocalDateTime;

import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class VerbrauchsabrechnungService {

    private static final Duration MAX_ABWEICHUNG =
            Duration.ofDays(1);

    private final VerbrauchskostenRepository verbrauchskostenRepository;
    private final ZaehlerRepository zaehlerRepository;
    private final ZaehlerstandRepository zaehlerstandRepository;
    private final BuchungVerbrauchskostenRepository
            buchungVerbrauchskostenRepository;

    public VerbrauchsabrechnungService(
            VerbrauchskostenRepository verbrauchskostenRepository,
            ZaehlerRepository zaehlerRepository,
            ZaehlerstandRepository zaehlerstandRepository,
            BuchungVerbrauchskostenRepository
                    buchungVerbrauchskostenRepository
    ) {
        this.verbrauchskostenRepository = verbrauchskostenRepository;
        this.zaehlerRepository = zaehlerRepository;
        this.zaehlerstandRepository = zaehlerstandRepository;
        this.buchungVerbrauchskostenRepository =
                buchungVerbrauchskostenRepository;
    }

    /**
     * Berechnet und speichert die endgültigen Verbrauchskosten.
     *
     * Airbnb-Buchungen werden nicht verbrauchsbezogen abgerechnet.
     * Bei Direktbuchungen müssen sämtliche Berechnungen erfolgreich sein,
     * bevor vorhandene Snapshots ersetzt werden.
     */
    public void berechneUndSpeichere(Buchung buchung) {
        berechneUndSpeichere(buchung, List.of());
    }

    public void berechneUndSpeichere(Buchung buchung, List<AblesungBestaetigungDTO> bestaetigungen) {

        var snapshots=berechneSnapshots(buchung,bestaetigungen);
        if(buchung.getBuchungsquelle()==Buchungsquelle.AIRBNB)return;
        buchungVerbrauchskostenRepository.deleteByBuchungId(buchung.getId());
        buchungVerbrauchskostenRepository.flush();
        buchungVerbrauchskostenRepository.saveAll(snapshots);
        buchungVerbrauchskostenRepository.flush();
    }

    /** Same validated computation for preview and commit, without writes. */
    @Transactional(readOnly=true)
    public List<BuchungVerbrauchskosten> berechneSnapshots(Buchung buchung,List<AblesungBestaetigungDTO> bestaetigungen) {
        if(buchung.getBuchungsquelle()==Buchungsquelle.AIRBNB)return List.of();

        if (buchung.getAnreise() == null || buchung.getAbreise() == null) {
            throw new IllegalArgumentException(
                    "Für die Verbrauchsabrechnung müssen Anreise "
                            + "und Abreise angegeben sein."
            );
        }

        if (buchung.getAbreise().getYear() >= 2999) {
            throw new IllegalArgumentException(
                    "Eine unbefristete Buchung kann erst abgeschlossen werden, "
                            + "wenn ein tatsächliches Abreisedatum eingetragen wurde."
            );
        }

        if (!buchung.getAbreise().isAfter(buchung.getAnreise())) {
            throw new IllegalArgumentException(
                    "Der Abreisezeitpunkt muss nach dem Anreisezeitpunkt liegen."
            );
        }
        LocalDateTime anreiseZeitpunkt = buchung.getAnreise();
        LocalDateTime abreiseZeitpunkt = buchung.getAbreise();

        int abrechnungstage = Math.max(1, Math.toIntExact(
                ChronoUnit.DAYS.between(
                        buchung.getAnreise().toLocalDate(),
                        buchung.getAbreise().toLocalDate()
                )
        ));

        List<Verbrauchskosten> konfigurationen =
                verbrauchskostenRepository
                        .findByMietobjektIdOrderByIdAsc(
                                buchung.getMietobjekt().getId()
                        )
                        .stream()
                        .filter(Verbrauchskosten::isAktiv)
                        .toList();

        List<BuchungVerbrauchskosten> snapshots = new ArrayList<>();

        /*
         * Erst alle Positionen berechnen.
         * Bei einem Fehler wird noch nichts gespeichert oder gelöscht.
         */
        for (Verbrauchskosten konfiguration : konfigurationen) {

            if (konfiguration.getAbrechnungsart()
                    == com.kcserver.vermietung.enumtype.Verbrauchsabrechnungsart.ZAEHLERSTAND) {

                List<Zaehler> passendeZaehler =
                        ermittleZugeordneteZaehler(buchung).stream()
                                .filter(z -> z.getVerbrauchsart()
                                        == konfiguration.getVerbrauchsart())
                                .toList();

                if (passendeZaehler.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Kein aktiver Zähler für "
                                    + konfiguration.getVerbrauchsart()
                                    + " ist einem gebuchten Mietbereich zugeordnet."
                    );
                }

                for (Zaehler zaehler : passendeZaehler) {
                    snapshots.add(
                            erstelleZaehlerSnapshot(
                                    buchung,
                                    konfiguration,
                                    zaehler,
                                    anreiseZeitpunkt,
                                    abreiseZeitpunkt,
                                    bestaetigungen == null ? List.of() : bestaetigungen
                            )
                    );
                }

            } else {
                snapshots.add(
                        erstelleSnapshot(
                                buchung,
                                konfiguration,
                                anreiseZeitpunkt,
                                abreiseZeitpunkt,
                                abrechnungstage
                        )
                );
            }
        }

        return List.copyOf(snapshots);
    }

    /**
     * Liefert alle aktiven Zähler, die einem der gebuchten Mietbereiche
     * zugeordnet sind. Jeder Zähler wird nur einmal berücksichtigt.
     */
    private List<Zaehler> ermittleZugeordneteZaehler(Buchung buchung) {

        Map<Long, Zaehler> zaehlerMap = new LinkedHashMap<>();

        for (BuchungMietbereich position : buchung.getMietbereiche()) {

            if (position.getMietbereich() == null
                    || position.getMietbereich().getZaehler() == null) {
                continue;
            }

            for (Zaehler zaehler : position.getMietbereich().getZaehler()) {

                if (!zaehler.isAktiv()) {
                    continue;
                }

                zaehlerMap.putIfAbsent(zaehler.getId(), zaehler);
            }
        }

        return new ArrayList<>(zaehlerMap.values());
    }

    private BuchungVerbrauchskosten erstelleZaehlerSnapshot(
            Buchung buchung,
            Verbrauchskosten konfiguration,
            Zaehler zaehler,
            LocalDateTime anreiseZeitpunkt,
            LocalDateTime abreiseZeitpunkt,
            List<AblesungBestaetigungDTO> bestaetigungen
    ) {
        BuchungVerbrauchskosten snapshot = new BuchungVerbrauchskosten();

        snapshot.setBuchung(buchung);
        snapshot.setVerbrauchskostenId(konfiguration.getId());
        snapshot.setBezeichnung(konfiguration.getBezeichnung());
        snapshot.setVerbrauchsart(konfiguration.getVerbrauchsart());
        snapshot.setAbrechnungsart(konfiguration.getAbrechnungsart());
        snapshot.setMengeneinheit(konfiguration.getMengeneinheit());
        snapshot.setPreis(konfiguration.getPreis());

        snapshot.setZaehlerId(zaehler.getId());
        snapshot.setZaehlerBezeichnung(zaehler.getBezeichnung());

        berechneZaehlerkosten(
                buchung,
                konfiguration,
                zaehler,
                snapshot,
                anreiseZeitpunkt,
                abreiseZeitpunkt,
                bestaetigungen
        );

        return snapshot;
    }

    /**
     * Berechnet eine aktuelle Vorschau der Verbrauchskosten,
     * ohne Datenbankeinträge anzulegen oder zu verändern.
     *
     * Fehler werden je Position zurückgegeben, damit die übrigen
     * Positionen weiterhin angezeigt werden können.
     */
    @Transactional(readOnly = true)
    public List<VerbrauchskostenVorschauDTO> berechneVorschau(
            Buchung buchung
    ) {
        List<VerbrauchskostenVorschauDTO> ergebnis = new ArrayList<>();

        if (buchung.getBuchungsquelle() == Buchungsquelle.AIRBNB) {
            return ergebnis;
        }

        if (buchung.getAnreise() == null || buchung.getAbreise() == null) {
            ergebnis.add(fehlerVorschau(
                    null,
                    null,
                    "Verbrauchskosten",
                    null,
                    null,
                    null,
                    null,
                    ErrorMessages.VERBRAUCHSABRECHNUNG_ANREISE_ABREISE_REQUIRED
            ));
            return ergebnis;
        }

        if (buchung.getAbreise().getYear() >= 2999) {
            ergebnis.add(fehlerVorschau(
                    null,
                    null,
                    "Verbrauchskosten",
                    null,
                    null,
                    null,
                    null,
                    ErrorMessages.VERBRAUCHSABRECHNUNG_VORSCHAU_UNBEFRISTET
            ));
            return ergebnis;
        }

        if (!buchung.getAbreise().isAfter(buchung.getAnreise())) {
            ergebnis.add(fehlerVorschau(
                    null,
                    null,
                    "Verbrauchskosten",
                    null,
                    null,
                    null,
                    null,
                    ErrorMessages.VERBRAUCHSABRECHNUNG_ABREISE_INVALID
            ));
            return ergebnis;
        }

        LocalDateTime anreiseZeitpunkt = buchung.getAnreise();
        LocalDateTime abreiseZeitpunkt = buchung.getAbreise();

        int abrechnungstage = Math.max(1, Math.toIntExact(
                ChronoUnit.DAYS.between(
                        buchung.getAnreise().toLocalDate(),
                        buchung.getAbreise().toLocalDate()
                )
        ));

        List<Verbrauchskosten> konfigurationen =
                verbrauchskostenRepository
                        .findByMietobjektIdOrderByIdAsc(
                                buchung.getMietobjekt().getId()
                        )
                        .stream()
                        .filter(Verbrauchskosten::isAktiv)
                        .toList();

        /*
         * Die gebuchten Mietbereiche bestimmen, welche Zähler
         * für diese Buchung relevant sind.
         *
         * LinkedHashMap dedupliziert die Zähler anhand ihrer ID
         * und erhält gleichzeitig eine stabile Reihenfolge.
         */
        Map<Long, Zaehler> zugeordneteZaehler = new LinkedHashMap<>();

        for (BuchungMietbereich position : buchung.getMietbereiche()) {
            Mietbereich mietbereich = position.getMietbereich();

            if (mietbereich == null || mietbereich.getZaehler() == null) {
                continue;
            }

            mietbereich.getZaehler().stream()
                    .filter(Zaehler::isAktiv)
                    .forEach(z -> zugeordneteZaehler.putIfAbsent(
                            z.getId(),
                            z
                    ));
        }

        for (Verbrauchskosten konfiguration : konfigurationen) {

            switch (konfiguration.getAbrechnungsart()) {

                case PAUSCHALE -> ergebnis.add(
                        erstelleEinfacheVorschau(
                                konfiguration,
                                konfiguration.getPreis(),
                                null
                        )
                );

                case TAGESPREIS -> {
                    BigDecimal kosten = konfiguration.getPreis()
                            .multiply(BigDecimal.valueOf(abrechnungstage))
                            .setScale(2, RoundingMode.HALF_UP);

                    ergebnis.add(
                            erstelleEinfacheVorschau(
                                    konfiguration,
                                    kosten,
                                    abrechnungstage
                            )
                    );
                }

                case ZAEHLERSTAND -> {
                    List<Zaehler> passendeZaehler =
                            zugeordneteZaehler.values().stream()
                                    .filter(z -> z.getVerbrauchsart()
                                            == konfiguration.getVerbrauchsart())
                                    .toList();

                    if (passendeZaehler.isEmpty()) {
                        ergebnis.add(fehlerVorschau(
                                null,
                                null,
                                konfiguration.getBezeichnung(),
                                konfiguration.getVerbrauchsart(),
                                konfiguration.getAbrechnungsart(),
                                konfiguration.getMengeneinheit(),
                                konfiguration.getPreis(),
                                ErrorMessages.VERBRAUCHSABRECHNUNG_VORSCHAU_ZAEHLER_FEHLT
                        ));
                        continue;
                    }

                    for (Zaehler zaehler : passendeZaehler) {
                        try {
                            ergebnis.add(
                                    berechneZaehlerVorschau(
                                            konfiguration,
                                            zaehler,
                                            anreiseZeitpunkt,
                                            abreiseZeitpunkt
                                    )
                            );
                        } catch (IllegalArgumentException ex) {
                            ergebnis.add(fehlerVorschau(
                                    zaehler.getId(),
                                    zaehler.getBezeichnung(),
                                    konfiguration.getBezeichnung(),
                                    konfiguration.getVerbrauchsart(),
                                    konfiguration.getAbrechnungsart(),
                                    konfiguration.getMengeneinheit(),
                                    konfiguration.getPreis(),
                                    ex.getMessage()
                            ));
                        }
                    }
                }
            }
        }

        return ergebnis;
    }

    private VerbrauchskostenVorschauDTO erstelleEinfacheVorschau(
            Verbrauchskosten konfiguration,
            BigDecimal kosten,
            Integer abrechnungstage
    ) {
        return new VerbrauchskostenVorschauDTO(
                null,
                null,
                konfiguration.getBezeichnung(),
                konfiguration.getVerbrauchsart(),
                konfiguration.getAbrechnungsart(),
                konfiguration.getMengeneinheit(),
                konfiguration.getPreis(),
                null,
                null,
                null,
                null,
                null,
                abrechnungstage,
                kosten.setScale(2, RoundingMode.HALF_UP),
                true,
                null
        );
    }


    private VerbrauchskostenVorschauDTO berechneZaehlerVorschau(
            Verbrauchskosten konfiguration,
            Zaehler zaehler,
            LocalDateTime anreiseZeitpunkt,
            LocalDateTime abreiseZeitpunkt
    ) {
        List<Zaehlerstand> staende =
                zaehlerstandRepository
                        .findByZaehlerIdOrderByAblesedatumDesc(
                                zaehler.getId()
                        );

        Ablesepaar paar = findeAblesepaar(staende, anreiseZeitpunkt, abreiseZeitpunkt, zaehler.getBezeichnung());
        Zaehlerstand anfang = paar.anfang();
        Zaehlerstand ende = paar.ende();

        if (anfang.getId().equals(ende.getId())) {
            throw new IllegalArgumentException(
                    String.format(
                            Locale.GERMAN,
                            ErrorMessages.VERBRAUCHSABRECHNUNG_GLEICHER_ZAEHLERSTAND,
                            zaehler.getBezeichnung()
                    )
            );
        }

        if (!anfang.getAblesedatum().isBefore(ende.getAblesedatum())) {
            throw new IllegalArgumentException(
                    String.format(
                            Locale.GERMAN,
                            ErrorMessages.VERBRAUCHSABRECHNUNG_ZAEHLERSTAND_REIHENFOLGE,
                            zaehler.getBezeichnung()
                    )
            );
        }

        BigDecimal verbrauch = ende.getZaehlerstand()
                .subtract(anfang.getZaehlerstand());

        if (verbrauch.signum() < 0) {
            throw new IllegalArgumentException(
                    String.format(
                            Locale.GERMAN,
                            ErrorMessages.VERBRAUCHSABRECHNUNG_NEGATIVER_VERBRAUCH,
                            zaehler.getBezeichnung()
                    )
            );
        }

        BigDecimal kosten = verbrauch
                .multiply(konfiguration.getPreis())
                .setScale(2, RoundingMode.HALF_UP);

        boolean bestaetigungNoetig = abweichend(anfang, anreiseZeitpunkt) || abweichend(ende, abreiseZeitpunkt);
        return new VerbrauchskostenVorschauDTO(
                zaehler.getId(),
                zaehler.getBezeichnung(),
                konfiguration.getBezeichnung(),
                konfiguration.getVerbrauchsart(),
                konfiguration.getAbrechnungsart(),
                konfiguration.getMengeneinheit(),
                konfiguration.getPreis(),
                anfang.getZaehlerstand(),
                ende.getZaehlerstand(),
                verbrauch,
                anfang.getAblesedatum(),
                ende.getAblesedatum(),
                null,
                kosten,
                !bestaetigungNoetig,
                bestaetigungNoetig ? "Ablesungen mit mehr als 24 Stunden Abstand: Das Messintervall enthält auch Zeiten außerhalb der Buchung. Verwendung beim Abschluss bestätigen." : null,
                bestaetigungNoetig
        );
    }


    private VerbrauchskostenVorschauDTO fehlerVorschau(
            Long zaehlerId,
            String zaehlerBezeichnung,
            String bezeichnung,
            com.kcserver.vermietung.enumtype.Verbrauchsart verbrauchsart,
            com.kcserver.vermietung.enumtype.Verbrauchsabrechnungsart abrechnungsart,
            String mengeneinheit,
            BigDecimal preis,
            String hinweis
    ) {
        return new VerbrauchskostenVorschauDTO(
                zaehlerId,
                zaehlerBezeichnung,
                bezeichnung,
                verbrauchsart,
                abrechnungsart,
                mengeneinheit,
                preis,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                hinweis
        );
    }

    private BuchungVerbrauchskosten erstelleSnapshot(
            Buchung buchung,
            Verbrauchskosten konfiguration,
            LocalDateTime anreiseZeitpunkt,
            LocalDateTime abreiseZeitpunkt,
            int abrechnungstage
    ) {
        BuchungVerbrauchskosten snapshot =
                new BuchungVerbrauchskosten();

        snapshot.setBuchung(buchung);
        snapshot.setVerbrauchskostenId(konfiguration.getId());
        snapshot.setBezeichnung(konfiguration.getBezeichnung());
        snapshot.setVerbrauchsart(konfiguration.getVerbrauchsart());
        snapshot.setAbrechnungsart(konfiguration.getAbrechnungsart());
        snapshot.setMengeneinheit(konfiguration.getMengeneinheit());
        snapshot.setPreis(konfiguration.getPreis());

        BigDecimal kosten;

        switch (konfiguration.getAbrechnungsart()) {

            case PAUSCHALE -> {
                kosten = konfiguration.getPreis();
            }

            case TAGESPREIS -> {
                kosten = konfiguration.getPreis()
                        .multiply(BigDecimal.valueOf(abrechnungstage));

                snapshot.setAbrechnungstage(abrechnungstage);
            }

            case ZAEHLERSTAND -> throw new IllegalArgumentException(
                    "Zählerkosten müssen über erstelleZaehlerSnapshot() "
                            + "berechnet werden."
            );

            default -> throw new IllegalArgumentException(
                    String.format(
                            Locale.GERMAN,
                            ErrorMessages.VERBRAUCHSABRECHNUNG_UNBEKANNTE_ABRECHNUNGSART,
                            konfiguration.getAbrechnungsart()
                    )
            );
        }

        snapshot.setKosten(
                kosten.setScale(2, RoundingMode.HALF_UP)
        );

        return snapshot;
    }

    private void berechneZaehlerkosten(
            Buchung buchung,
            Verbrauchskosten konfiguration,
            Zaehler ausgewaehlterZaehler,
            BuchungVerbrauchskosten snapshot,
            LocalDateTime anreiseZeitpunkt,
            LocalDateTime abreiseZeitpunkt,
            List<AblesungBestaetigungDTO> bestaetigungen
    ) {
        List<Zaehlerstand> staende =
                zaehlerstandRepository
                        .findByZaehlerIdOrderByAblesedatumDesc(
                                ausgewaehlterZaehler.getId()
                        );

        String bezeichnung = ausgewaehlterZaehler.getBezeichnung();

        Ablesepaar paar = findeAblesepaar(staende, anreiseZeitpunkt, abreiseZeitpunkt, bezeichnung);
        Zaehlerstand anfang = paar.anfang();
        Zaehlerstand ende = paar.ende();

        if (anfang.getId().equals(ende.getId())) {
            throw new IllegalArgumentException(
                    "Für den Zähler „" + bezeichnung
                            + "“ wurde für Anreise und Abreise derselbe "
                            + "Zählerstand ausgewählt. Bitte weitere Ablesungen erfassen."
            );
        }

        if (!anfang.getAblesedatum().isBefore(ende.getAblesedatum())) {
            throw new IllegalArgumentException(
                    "Die Ablesung für die Anreise muss zeitlich vor "
                            + "der Ablesung für die Abreise liegen. "
                            + "Bitte den Zähler „" + bezeichnung + "“ prüfen."
            );
        }

        if (abweichend(anfang, anreiseZeitpunkt) || abweichend(ende, abreiseZeitpunkt)) {
            boolean bestaetigt = bestaetigungen.stream().anyMatch(b ->
                    Objects.equals(b.zaehlerId(), ausgewaehlterZaehler.getId())
                    && Objects.equals(b.anreise(), anreiseZeitpunkt)
                    && Objects.equals(b.abreise(), abreiseZeitpunkt)
                    && Objects.equals(b.anfangsablesung(), anfang.getAblesedatum())
                    && Objects.equals(b.endablesung(), ende.getAblesedatum())
                    && b.anfangsstand() != null && b.anfangsstand().compareTo(anfang.getZaehlerstand()) == 0
                    && b.endstand() != null && b.endstand().compareTo(ende.getZaehlerstand()) == 0);
            if (!bestaetigt) {
                throw new IllegalArgumentException("Die nächstliegenden Ablesungen für „" + bezeichnung
                        + "“ liegen mehr als 24 Stunden entfernt. Bitte die aktuelle Vorschau prüfen und ihre Verwendung bestätigen.");
            }
        }

        BigDecimal verbrauch = ende.getZaehlerstand()
                .subtract(anfang.getZaehlerstand());

        if (verbrauch.signum() < 0) {
            throw new IllegalArgumentException(
                    "Der Zählerstand von „" + bezeichnung
                            + "“ ist gesunken. Bitte prüfen, ob der Zähler "
                            + "zurückgesetzt oder ausgetauscht wurde."
            );
        }

        BigDecimal kosten = verbrauch
                .multiply(konfiguration.getPreis())
                .setScale(2, RoundingMode.HALF_UP);

        snapshot.setZaehlerId(ausgewaehlterZaehler.getId());
        snapshot.setZaehlerBezeichnung(bezeichnung);
        snapshot.setAnfangsstand(anfang.getZaehlerstand());
        snapshot.setEndstand(ende.getZaehlerstand());
        snapshot.setVerbrauch(verbrauch);
        snapshot.setAnfangsablesung(anfang.getAblesedatum());
        snapshot.setEndablesung(ende.getAblesedatum());
        snapshot.setKosten(kosten);
    }

    private record Ablesepaar(Zaehlerstand anfang, Zaehlerstand ende) {}

    private Ablesepaar findeAblesepaar(List<Zaehlerstand> staende,
                                      LocalDateTime anreise, LocalDateTime abreise,
                                      String bezeichnung) {
        Zaehlerstand anfang = findeNaechstenStand(staende, anreise, bezeichnung);
        Zaehlerstand ende = findeNaechstenStand(staende, abreise, bezeichnung);
        if (anfang.getAblesedatum().isBefore(ende.getAblesedatum())) {
            return new Ablesepaar(anfang, ende);
        }

        // Bei derselben nächstliegenden Ablesung die besser passende Buchungsgrenze
        // beibehalten und für die andere Grenze eine zeitlich getrennte Ablesung suchen.
        LocalDateTime gemeinsam = anfang.getAblesedatum();
        List<Zaehlerstand> davor = staende.stream()
                .filter(s -> s.getAblesedatum().isBefore(gemeinsam)).toList();
        List<Zaehlerstand> danach = staende.stream()
                .filter(s -> s.getAblesedatum().isAfter(gemeinsam)).toList();
        boolean endeBeibehalten = Duration.between(abreise, gemeinsam).abs()
                .compareTo(Duration.between(anreise, gemeinsam).abs()) <= 0;
        if (!davor.isEmpty() && (endeBeibehalten || danach.isEmpty())) {
            return new Ablesepaar(findeNaechstenStand(davor, anreise, bezeichnung), ende);
        }
        if (!danach.isEmpty()) {
            return new Ablesepaar(anfang, findeNaechstenStand(danach, abreise, bezeichnung));
        }
        // Nur eine Ablesung bzw. mehrere am selben Zeitpunkt: weiterhin nicht abrechenbar.
        return new Ablesepaar(anfang, ende);
    }

    private boolean abweichend(Zaehlerstand stand, LocalDateTime soll) {
        return Duration.between(soll, stand.getAblesedatum()).abs().compareTo(MAX_ABWEICHUNG) > 0;
    }

    private Zaehlerstand findeNaechstenStand(
            List<Zaehlerstand> staende,
            LocalDateTime sollZeitpunkt,
            String bezeichnung
    ) {
        if (staende.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format(
                            Locale.GERMAN,
                            ErrorMessages.VERBRAUCHSABRECHNUNG_ZAEHLERSTAENDE_FEHLEN,
                            bezeichnung
                    )
            );
        }

        Zaehlerstand naechsterStand = staende.stream()
                .min(Comparator.<Zaehlerstand, Duration>comparing(
                        stand -> Duration.between(
                                sollZeitpunkt,
                                stand.getAblesedatum()
                        ).abs()
                ).thenComparing(Zaehlerstand::getAblesedatum)
                 .thenComparing(Zaehlerstand::getId))
                .orElseThrow();

        return naechsterStand;
    }
}
