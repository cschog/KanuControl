package com.kcserver.vermietung.service;

import com.kcserver.vermietung.dto.VerbrauchskostenVorschauDTO;
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
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class VerbrauchsabrechnungService {

    private static final LocalTime DEFAULT_ANREISEZEIT =
            LocalTime.of(16, 0);

    private static final LocalTime DEFAULT_ABREISEZEIT =
            LocalTime.of(14, 0);

    private static final Duration MAX_ABWEICHUNG =
            Duration.ofDays(1);

    private static final BigDecimal NULLBETRAG =
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

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

        if (buchung.getBuchungsquelle() == Buchungsquelle.AIRBNB) {
            return;
        }

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
                    "Das Abreisedatum muss nach dem Anreisedatum liegen."
            );
        }

        LocalDateTime anreiseZeitpunkt = buchung.getAnreise().atTime(
                buchung.getMietobjekt().getAnreisezeit() != null
                        ? buchung.getMietobjekt().getAnreisezeit()
                        : DEFAULT_ANREISEZEIT
        );

        LocalDateTime abreiseZeitpunkt = buchung.getAbreise().atTime(
                buchung.getMietobjekt().getAbreisezeit() != null
                        ? buchung.getMietobjekt().getAbreisezeit()
                        : DEFAULT_ABREISEZEIT
        );

        int abrechnungstage = Math.toIntExact(
                ChronoUnit.DAYS.between(
                        buchung.getAnreise(),
                        buchung.getAbreise()
                )
        );

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
                                    abreiseZeitpunkt
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

        buchungVerbrauchskostenRepository.deleteByBuchungId(
                buchung.getId()
        );

        buchungVerbrauchskostenRepository.saveAll(snapshots);
        buchungVerbrauchskostenRepository.flush();
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

                if (!zaehler.isAktiv()
                        || zaehler.getMietobjekt() == null
                        || !zaehler.getMietobjekt().getId().equals(
                        buchung.getMietobjekt().getId()
                )) {
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
            LocalDateTime abreiseZeitpunkt
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
                abreiseZeitpunkt
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

        LocalDateTime anreiseZeitpunkt = buchung.getAnreise().atTime(
                buchung.getMietobjekt().getAnreisezeit() != null
                        ? buchung.getMietobjekt().getAnreisezeit()
                        : DEFAULT_ANREISEZEIT
        );

        LocalDateTime abreiseZeitpunkt = buchung.getAbreise().atTime(
                buchung.getMietobjekt().getAbreisezeit() != null
                        ? buchung.getMietobjekt().getAbreisezeit()
                        : DEFAULT_ABREISEZEIT
        );

        int abrechnungstage = Math.toIntExact(
                ChronoUnit.DAYS.between(
                        buchung.getAnreise(),
                        buchung.getAbreise()
                )
        );

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
                    .filter(z -> z.getMietobjekt() != null)
                    .filter(z -> z.getMietobjekt().getId().equals(
                            buchung.getMietobjekt().getId()
                    ))
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

        Zaehlerstand anfang = findeNaechstenStand(
                staende,
                anreiseZeitpunkt,
                "Anreise",
                zaehler.getBezeichnung()
        );

        Zaehlerstand ende = findeNaechstenStand(
                staende,
                abreiseZeitpunkt,
                "Abreise",
                zaehler.getBezeichnung()
        );

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
                true,
                null
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
            LocalDateTime abreiseZeitpunkt
    ) {
        List<Zaehlerstand> staende =
                zaehlerstandRepository
                        .findByZaehlerIdOrderByAblesedatumDesc(
                                ausgewaehlterZaehler.getId()
                        );

        String bezeichnung = ausgewaehlterZaehler.getBezeichnung();

        Zaehlerstand anfang = findeNaechstenStand(
                staende,
                anreiseZeitpunkt,
                "Anreise",
                bezeichnung
        );

        Zaehlerstand ende = findeNaechstenStand(
                staende,
                abreiseZeitpunkt,
                "Abreise",
                bezeichnung
        );

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

    private Zaehlerstand findeNaechstenStand(
            List<Zaehlerstand> staende,
            LocalDateTime sollZeitpunkt,
            String vorgang,
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
                .min(Comparator.comparing(
                        stand -> Duration.between(
                                sollZeitpunkt,
                                stand.getAblesedatum()
                        ).abs()
                ))
                .orElseThrow();

        Duration abweichung = Duration.between(
                sollZeitpunkt,
                naechsterStand.getAblesedatum()
        ).abs();

        if (abweichung.compareTo(MAX_ABWEICHUNG) > 0) {
            throw new IllegalArgumentException(
                    String.format(
                            Locale.GERMAN,
                            ErrorMessages.VERBRAUCHSABRECHNUNG_ZAEHLERSTAND_ABWEICHUNG,
                            bezeichnung,
                            vorgang,
                            sollZeitpunkt,
                            naechsterStand.getAblesedatum()
                    )
            );
        }

        return naechsterStand;
    }
}