package com.kcserver.vermietung.service;

import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.core.entity.Person;
import com.kcserver.core.entity.Verein;
import com.kcserver.kjfp.repository.PersonRepository;
import com.kcserver.core.repository.VereinRepository;
import com.kcserver.vermietung.dto.BuchungDTO;
import com.kcserver.vermietung.dto.BuchungMietbereichDTO;
import com.kcserver.vermietung.dto.BuchungVerbrauchskostenDTO;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsquelle;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.enumtype.Rabattart;
import com.kcserver.vermietung.enumtype.RabattwertTyp;
import com.kcserver.vermietung.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
@Transactional
public class BuchungService {

    private final BuchungRepository buchungRepository;
    private final BuchungsnummerCounterRepository
            buchungsnummerCounterRepository;
    private final PersonRepository personRepository;
    private final VereinRepository vereinRepository;
    private final MietobjektRepository mietobjektRepository;
    private final MietbereichRepository mietbereichRepository;
    private final MietpreisRepository mietpreisRepository;
    private final MietbereichRabattRepository mietbereichRabattRepository;
    private final ZaehlerRepository zaehlerRepository;
    private final ZaehlerstandRepository zaehlerstandRepository;
    private final VerbrauchsabrechnungService
            verbrauchsabrechnungService;
    private final BuchungVerbrauchskostenRepository
            buchungVerbrauchskostenRepository;

    private static final LocalDateTime UNBEFRISTET_BIS =
            LocalDateTime.of(3000, 12, 31, 14, 0);

    public BuchungService(
            BuchungRepository buchungRepository,
            BuchungsnummerCounterRepository buchungsnummerCounterRepository,
            PersonRepository personRepository,
            VereinRepository vereinRepository,
            MietobjektRepository mietobjektRepository,
            MietbereichRepository mietbereichRepository,
            MietpreisRepository mietpreisRepository,
            MietbereichRabattRepository mietbereichRabattRepository,
            ZaehlerRepository zaehlerRepository,
            ZaehlerstandRepository zaehlerstandRepository,
            VerbrauchsabrechnungService verbrauchsabrechnungService,
            BuchungVerbrauchskostenRepository buchungVerbrauchskostenRepository
    ) {
        this.buchungRepository = buchungRepository;
        this.buchungsnummerCounterRepository =
                buchungsnummerCounterRepository;
        this.personRepository = personRepository;
        this.vereinRepository = vereinRepository;
        this.mietobjektRepository = mietobjektRepository;
        this.mietbereichRepository = mietbereichRepository;
        this.mietpreisRepository = mietpreisRepository;
        this.mietbereichRabattRepository = mietbereichRabattRepository;
        this.zaehlerRepository = zaehlerRepository;
        this.zaehlerstandRepository = zaehlerstandRepository;
        this.verbrauchsabrechnungService = verbrauchsabrechnungService;
        this.buchungVerbrauchskostenRepository =
                buchungVerbrauchskostenRepository;
    }

    @Transactional(readOnly = true)
    public List<BuchungDTO> getAll() {
        return buchungRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public BuchungDTO getById(Long id) {

        Buchung buchung = buchungRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                String.format(
                                        java.util.Locale.GERMAN,
                                        ErrorMessages.BUCHUNG_NOT_FOUND,
                                        id
                                )
                        )
                );

        // Prüfen, ob eine bestätigte Buchung abrechenbar geworden ist.
        if (buchung.getStatus() == Buchungsstatus.BESTAETIGT
                && buchung.getAbreise() != null
                && buchung.getAbreise().getYear() < 2999
                && !buchung.getAbreise().isAfter(LocalDateTime.now())) {

            buchung.setStatus(Buchungsstatus.ABRECHENBAR);

            buchung = buchungRepository.save(buchung);
        }

        // Erst nach der Statusprüfung das DTO erstellen.
        BuchungDTO dto = toDTO(buchung);

        // Vorschau nur bei abrechenbaren Buchungen berechnen.
        if (buchung.getStatus() == Buchungsstatus.ABRECHENBAR) {
            dto.setVerbrauchskostenVorschau(
                    verbrauchsabrechnungService.berechneVorschau(buchung)
            );
        }

        return dto;
    }

    public BuchungDTO create(BuchungDTO dto) {

        Person mieter = getMieter(dto.getMieterId());

        Mietobjekt mietobjekt =
                getMietobjekt(dto.getMietobjektId());

        Verein veranstalter =
                getVeranstalter(dto.getVeranstalterVereinId());

        Buchung buchung = new Buchung();

        buchung.setBuchungsquelle(
                dto.getBuchungsquelle() != null
                        ? dto.getBuchungsquelle()
                        : Buchungsquelle.DIREKT
        );

        buchung.setMieter(mieter);
        buchung.setMietobjekt(mietobjekt);
        buchung.setVeranstalter(veranstalter);
        buchung.setBemerkung(dto.getBemerkung());
        buchung.setBewertung(dto.getBewertung());

        boolean unbefristet = dto.isUnbefristet();

        // Die Standardzeiten müssen am Mietobjekt konfiguriert sein.
        if (mietobjekt.getAnreisezeit() == null) {
            throw new IllegalArgumentException(
                    "Für das Mietobjekt \""
                            + mietobjekt.getBezeichnung()
                            + "\" ist keine Standard-Anreisezeit hinterlegt."
            );
        }

        if (!unbefristet && mietobjekt.getAbreisezeit() == null) {
            throw new IllegalArgumentException(
                    "Für das Mietobjekt \""
                            + mietobjekt.getBezeichnung()
                            + "\" ist keine Standard-Abreisezeit hinterlegt."
            );
        }

        // Datum und Uhrzeit werden vollständig vom Frontend übernommen.
        LocalDateTime anreise = dto.getAnreise();

        if (anreise == null) {
            throw new IllegalArgumentException(
                    "Bitte gib Datum und Uhrzeit der Anreise an."
            );
        }

        LocalDateTime abreise;

        if (unbefristet) {
            abreise = UNBEFRISTET_BIS;
        } else {
            abreise = dto.getAbreise();

            if (abreise == null) {
                throw new IllegalArgumentException(
                        "Bitte gib Datum und Uhrzeit der Abreise an."
                );
            }

            if (!abreise.isAfter(anreise)) {
                throw new IllegalArgumentException(
                        "Die Abreise muss nach der Anreise liegen."
                );
            }
        }

        buchung.setAnreise(anreise);
        buchung.setAbreise(abreise);

        buchung.setStatus(Buchungsstatus.ANFRAGE);

        vergebeBuchungsnummer(buchung);

        // Zuerst speichern, damit createdAt sicher gesetzt ist.
        buchung = buchungRepository.saveAndFlush(buchung);

        // Anschließend Positionen einschließlich Preissnapshots anlegen.
        synchronisiereBuchungspositionen(
                buchung,
                dto,
                mietobjekt,
                buchung.getBuchungsquelle()
        );

        validiereMietbereichVoraussetzungen(buchung);
        berechneBuchungsrabatt(buchung, dto, false);

        buchungRepository.flush();

        return toDTO(buchung);
    }

    public BuchungDTO update(Long id, BuchungDTO dto) {

        Buchung buchung = buchungRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                String.format(
                                        Locale.GERMAN,
                                        ErrorMessages.BUCHUNG_NOT_FOUND,
                                        id
                                )
                        )
                );

        // Der Status darf nicht über die allgemeine Aktualisierung
        // geändert werden.
        if (dto.getStatus() != null
                && dto.getStatus() != buchung.getStatus()) {
            throw new IllegalArgumentException(
                    "Der Buchungsstatus darf nicht über die "
                            + "allgemeine Aktualisierung geändert werden. "
                            + "Verwende dafür die Statusänderung."
            );
        }

        if (dto.getBewertung() != null
                && (dto.getBewertung() < 1 || dto.getBewertung() > 5)) {
            throw new IllegalArgumentException(
                    "Die Bewertung muss zwischen 1 und 5 liegen."
            );
        }

        // Stornierte und abgelehnte Buchungen dürfen ausschließlich
        // bei Bemerkung und Bewertung geändert werden.
        if (buchung.getStatus() == Buchungsstatus.STORNIERT
                || buchung.getStatus() == Buchungsstatus.ABGELEHNT) {

            if (!sindBuchungsdatenUnveraendert(buchung, dto)) {
                throw new IllegalArgumentException(
                        "Bei stornierten oder abgelehnten Buchungen "
                                + "dürfen nur Bemerkung und Bewertung "
                                + "geändert werden."
                );
            }

            buchung.setBemerkung(dto.getBemerkung());
            buchung.setBewertung(dto.getBewertung());

            return toDTO(buchungRepository.save(buchung));
        }

        boolean rabattAuswahlUnveraendert =
                Objects.equals(buchung.getRabattart(), dto.getRabattart())
                        && Objects.equals(buchung.getRabattwertTyp(), dto.getRabattwertTyp())
                        && Objects.equals(buchung.getRabattwert(), dto.getRabattwert());

        Person mieter = getMieter(dto.getMieterId());

        Mietobjekt mietobjekt =
                getMietobjekt(dto.getMietobjektId());

        Verein veranstalter =
                getVeranstalter(dto.getVeranstalterVereinId());

        // Die Buchungsquelle bleibt nach dem Anlegen unveränderlich.
        if (dto.getBuchungsquelle() != null
                && dto.getBuchungsquelle() != buchung.getBuchungsquelle()) {
            throw new IllegalArgumentException(
                    "Die Buchungsquelle kann nach dem Anlegen "
                            + "nicht mehr geändert werden."
            );
        }

        buchung.setMieter(mieter);
        buchung.setMietobjekt(mietobjekt);
        buchung.setVeranstalter(veranstalter);

        buchung.setBemerkung(dto.getBemerkung());
        buchung.setBewertung(dto.getBewertung());

        boolean unbefristet = dto.isUnbefristet();

        LocalDateTime anreise = dto.getAnreise();
        LocalDateTime abreise = unbefristet
                ? UNBEFRISTET_BIS
                : dto.getAbreise();

        pruefeBuchungszeiten(
                mietobjekt,
                anreise,
                abreise,
                unbefristet
        );

        buchung.setAnreise(anreise);
        buchung.setAbreise(abreise);

        // Auch bei ABGESCHLOSSEN bleibt der Status unverändert.
        // Die Buchung kann korrigiert und später ausdrücklich
        // erneut abgeschlossen werden.

        synchronisiereBuchungspositionen(
                buchung,
                dto,
                mietobjekt,
                buchung.getBuchungsquelle()
        );

        validiereMietbereichVoraussetzungen(buchung);

        if (buchung.getStatus() == Buchungsstatus.RESERVIERT
                || buchung.getStatus() == Buchungsstatus.BESTAETIGT) {
            pruefeBestand(buchung, buchung.getId());
        }

        berechneBuchungsrabatt(buchung, dto, rabattAuswahlUnveraendert);

        buchungRepository.flush();

        return toDTO(buchung);
    }

    private void pruefeBuchungszeiten(
            Mietobjekt mietobjekt,
            LocalDateTime anreise,
            LocalDateTime abreise,
            boolean unbefristet
    ) {
        if (mietobjekt.getAnreisezeit() == null) {
            throw new IllegalArgumentException(
                    "Für das Mietobjekt \""
                            + mietobjekt.getBezeichnung()
                            + "\" ist keine Standard-Anreisezeit hinterlegt."
            );
        }

        if (!unbefristet && mietobjekt.getAbreisezeit() == null) {
            throw new IllegalArgumentException(
                    "Für das Mietobjekt \""
                            + mietobjekt.getBezeichnung()
                            + "\" ist keine Standard-Abreisezeit hinterlegt."
            );
        }

        if (anreise == null) {
            throw new IllegalArgumentException(
                    "Bitte gib Datum und Uhrzeit der Anreise an."
            );
        }

        if (unbefristet) {
            return;
        }

        if (abreise == null) {
            throw new IllegalArgumentException(
                    "Bitte gib Datum und Uhrzeit der Abreise an."
            );
        }

        if (!abreise.isAfter(anreise)) {
            throw new IllegalArgumentException(
                    "Die Abreise muss nach der Anreise liegen."
            );
        }
    }

    private boolean sindBuchungsdatenUnveraendert(
            Buchung buchung,
            BuchungDTO dto
    ) {
        if (dto.getMieterId() == null
                || dto.getMietobjektId() == null
                || dto.getAnreise() == null) {
            return false;
        }

        LocalDateTime gewuenschteAbreise = dto.isUnbefristet()
                ? UNBEFRISTET_BIS
                : dto.getAbreise();

        if (!Objects.equals(
                buchung.getMieter().getId(),
                dto.getMieterId())) {
            return false;
        }

        if (!Objects.equals(
                buchung.getMietobjekt().getId(),
                dto.getMietobjektId())) {
            return false;
        }

        Long bisherigerVeranstalterId =
                buchung.getVeranstalter() != null
                        ? buchung.getVeranstalter().getId()
                        : null;

        if (!Objects.equals(
                bisherigerVeranstalterId,
                dto.getVeranstalterVereinId())) {
            return false;
        }

        if (!Objects.equals(
                buchung.getAnreise(),
                dto.getAnreise())) {
            return false;
        }

        if (!Objects.equals(
                buchung.getAbreise(),
                gewuenschteAbreise)) {
            return false;
        }

        Map<Long, Integer> bestehendePositionen = new HashMap<>();

        for (BuchungMietbereich position : buchung.getMietbereiche()) {
            bestehendePositionen.put(
                    position.getMietbereich().getId(),
                    position.getAnzahl()
            );
        }

        Map<Long, Integer> gewuenschtePositionen = new HashMap<>();

        if (dto.getPositionen() != null) {
            for (BuchungMietbereichDTO position : dto.getPositionen()) {
                if (position.getMietbereichId() == null
                        || position.getAnzahl() == null
                        || position.getAnzahl() < 1
                        || gewuenschtePositionen.containsKey(
                        position.getMietbereichId())) {
                    return false;
                }

                gewuenschtePositionen.put(
                        position.getMietbereichId(),
                        position.getAnzahl()
                );
            }
        } else if (dto.getMietbereichIds() != null) {
            for (Long mietbereichId : dto.getMietbereichIds()) {
                if (mietbereichId == null
                        || gewuenschtePositionen.containsKey(mietbereichId)) {
                    return false;
                }

                gewuenschtePositionen.put(mietbereichId, 1);
            }
        }

        return bestehendePositionen.equals(gewuenschtePositionen);
    }

    public void delete(Long id) {

        Buchung buchung = buchungRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                String.format(
                                        java.util.Locale.GERMAN,
                                        ErrorMessages.BUCHUNG_NOT_FOUND,
                                        id
                                )
                        )
                );

        buchungRepository.delete(buchung);
    }

    private List<Mietbereich> getMietbereiche(
            List<Long> mietbereichIds,
            Mietobjekt mietobjekt
    ) {
        if (mietbereichIds == null || mietbereichIds.isEmpty()) {
            return List.of();
        }

        List<Mietbereich> mietbereiche =
                mietbereichRepository.findAllById(mietbereichIds);

        if (mietbereiche.size() != mietbereichIds.size()) {
            throw new IllegalArgumentException(
                    "Mindestens ein Mietbereich wurde nicht gefunden."
            );
        }

        boolean falschesMietobjekt = mietbereiche.stream()
                .anyMatch(bereich ->
                        !bereich.getMietobjekt().getId()
                                .equals(mietobjekt.getId())
                );

        if (falschesMietobjekt) {
            throw new IllegalArgumentException(
                    "Mindestens ein Mietbereich gehört nicht zum angegebenen Mietobjekt."
            );
        }

        boolean nichtMietbar = mietbereiche.stream()
                .anyMatch(bereich -> !bereich.isMietbar());

        if (nichtMietbar) {
            throw new IllegalArgumentException(
                    "Mindestens ein ausgewählter Mietbereich ist nicht mietbar."
            );
        }

        return mietbereiche;
    }

    /**
     * Vergibt die nächste Buchungsnummer für das aktuelle Jahr.
     */
    private void vergebeBuchungsnummer(Buchung buchung) {

        int jahr = Year.now().getValue();

        BuchungsnummerCounter counter =
                buchungsnummerCounterRepository.findByJahr(jahr)
                        .orElseGet(() -> {
                            BuchungsnummerCounter neuerCounter =
                                    new BuchungsnummerCounter();

                            neuerCounter.setJahr(jahr);
                            neuerCounter.setLetzteNummer(0);

                            return buchungsnummerCounterRepository.save(
                                    neuerCounter
                            );
                        });

        int naechsteNummer =
                counter.getLetzteNummer() + 1;

        counter.setLetzteNummer(naechsteNummer);

        buchungsnummerCounterRepository.save(counter);

        buchung.setBuchungsjahr(jahr);
        buchung.setLaufendeNummer(naechsteNummer);
    }

    private Person getMieter(Long mieterId) {

        return personRepository.findById(mieterId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Mieter nicht gefunden: " + mieterId
                        )
                );
    }

    private Mietobjekt getMietobjekt(Long mietobjektId) {

        return mietobjektRepository.findById(mietobjektId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Mietobjekt nicht gefunden: " + mietobjektId
                        )
                );
    }

    private Verein getVeranstalter(Long vereinId) {

        if (vereinId == null) {
            return null;
        }

        return vereinRepository.findById(vereinId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Veranstalter nicht gefunden: " + vereinId
                        )
                );
    }

    private BuchungDTO toDTO(Buchung buchung) {

        BuchungDTO dto = new BuchungDTO();

        dto.setId(buchung.getId());

        dto.setBuchungsnummer(
                buchung.getBuchungsnummer()
        );

        dto.setStatus(buchung.getStatus());
        dto.setBuchungsquelle(buchung.getBuchungsquelle());
        dto.setRabattart(buchung.getRabattart());
        dto.setRabattwertTyp(buchung.getRabattwertTyp());
        dto.setRabattwert(buchung.getRabattwert());
        dto.setRabattbetrag(buchung.getRabattbetrag());

        dto.setAnreise(buchung.getAnreise());
        dto.setAbreise(buchung.getAbreise());
        dto.setUnbefristet(
                UNBEFRISTET_BIS.equals(buchung.getAbreise())
        );

        dto.setMietobjektId(
                buchung.getMietobjekt().getId()
        );

        dto.setMietobjektBezeichnung(
                buchung.getMietobjekt().getBezeichnung()
        );

        dto.setMietbereichIds(
                buchung.getMietbereiche()
                        .stream()
                        .map(zuordnung ->
                                zuordnung.getMietbereich().getId()
                        )
                        .toList()
        );

        dto.setPositionen(
                buchung.getMietbereiche()
                        .stream()
                        .map(zuordnung -> {
                            BuchungMietbereichDTO position =
                                    new BuchungMietbereichDTO();

                            position.setMietbereichId(
                                    zuordnung.getMietbereich().getId()
                            );
                            position.setMietbereichBezeichnung(
                                    zuordnung.getMietbereich().getBezeichnung()
                            );
                            position.setMengeneinheit(
                                    zuordnung.getMietbereich().getMengeneinheit()
                            );
                            position.setAnzahl(zuordnung.getAnzahl());

                            position.setVereinbarterPreis(
                                    zuordnung.getVereinbarterPreis()
                            );

                            position.setBerechnungsart(
                                    zuordnung.getBerechnungsart()
                            );

                            position.setRabattwertTyp(
                                    zuordnung.getRabattwertTyp()
                            );
                            position.setRabattwert(zuordnung.getRabattwert());
                            position.setRabattbetrag(zuordnung.getRabattbetrag());

                            return position;
                        })
                        .toList()
        );

        dto.setMieterId(
                buchung.getMieter().getId()
        );

        dto.setMieterVorname(
                buchung.getMieter().getVorname()
        );

        dto.setMieterName(
                buchung.getMieter().getName()
        );

        dto.setVeranstalterVereinId(
                buchung.getVeranstalter() != null
                        ? buchung.getVeranstalter().getId()
                        : null
        );

        dto.setBemerkung(buchung.getBemerkung());
        dto.setBewertung(buchung.getBewertung());

        dto.setVerbrauchskosten(
                buchungVerbrauchskostenRepository
                        .findByBuchungIdOrderByIdAsc(buchung.getId())
                        .stream()
                        .map(kosten -> new BuchungVerbrauchskostenDTO(
                                kosten.getId(),
                                kosten.getVerbrauchskostenId(),
                                kosten.getBezeichnung(),
                                kosten.getVerbrauchsart(),
                                kosten.getAbrechnungsart(),
                                kosten.getMengeneinheit(),
                                kosten.getPreis(),
                                kosten.getAnfangsstand(),
                                kosten.getEndstand(),
                                kosten.getVerbrauch(),
                                kosten.getAnfangsablesung(),
                                kosten.getEndablesung(),
                                kosten.getAbrechnungstage(),
                                kosten.getKosten()
                        ))
                        .toList()
        );

        return dto;
    }

    @Transactional(readOnly = true)
    public java.math.BigDecimal getPreis(
            Long mietbereichId,
            Buchungsquelle buchungsquelle,
            LocalDate stichtag
    ) {
        Mietbereich mietbereich = mietbereichRepository.findById(mietbereichId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mietbereich nicht gefunden: " + mietbereichId
                ));

        Mietobjekt mietobjekt = mietbereich.getMietobjekt();

        pruefeBuchungsquelle(buchungsquelle, mietobjekt, mietbereich);

        return mietpreisRepository
                .findFirstByMietbereichIdAndBuchungsquelleAndGueltigAbLessThanEqualOrderByGueltigAbDesc(
                        mietbereichId,
                        buchungsquelle,
                        stichtag
                )
                .map(Mietpreis::getPreis)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Kein gültiger Mietpreis für den Mietbereich '"
                                + mietbereich.getBezeichnung()
                                + "' am "
                                + stichtag
                                + " vorhanden."
                ));
    }

    /**
     * Stellt bestätigte Buchungen nach Erreichen des Abreisezeitpunkts
     * auf ABRECHENBAR um.
     *
     * Unbefristete Buchungen werden nicht umgestellt.
     */
    public int aktualisiereAbrechenbareBuchungen() {
        LocalDateTime jetzt = LocalDateTime.now();

        List<Buchung> faelligeBuchungen =
                buchungRepository.findByStatusAndAbreiseLessThanEqual(
                        Buchungsstatus.BESTAETIGT,
                        jetzt
                );

        int anzahl = 0;

        for (Buchung buchung : faelligeBuchungen) {
            // Unbefristete Buchungen nicht automatisch umstellen.
            if (buchung.getAbreise() != null
                    && buchung.getAbreise().getYear() >= 2999) {
                continue;
            }

            buchung.setStatus(Buchungsstatus.ABRECHENBAR);
            anzahl++;
        }

        if (anzahl > 0) {
            buchungRepository.flush();
        }

        return anzahl;
    }

    public BuchungDTO aendereStatus(
            Long id,
            Buchungsstatus neuerStatus
    ) {
        Buchung buchung = buchungRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                String.format(
                                        Locale.GERMAN,
                                        ErrorMessages.BUCHUNG_NOT_FOUND,
                                        id
                                )
                        )
                );

        Buchungsstatus alterStatus = buchung.getStatus();

        if (neuerStatus == null) {
            throw new IllegalArgumentException(
                    "Der neue Buchungsstatus darf nicht null sein."
            );
        }

        if (!istStatusuebergangErlaubt(alterStatus, neuerStatus)) {
            throw new IllegalArgumentException(
                    "Der Statuswechsel von "
                            + alterStatus
                            + " nach "
                            + neuerStatus
                            + " ist nicht erlaubt."
            );
        }

        if (neuerStatus == Buchungsstatus.RESERVIERT
                || neuerStatus == Buchungsstatus.BESTAETIGT) {
            pruefeBestand(buchung, buchung.getId());
        }

        /*
         * Die Verbrauchsabrechnung muss erfolgreich sein,
         * bevor der Status geändert wird.
         */
        if (neuerStatus == Buchungsstatus.ABGESCHLOSSEN) {
            verbrauchsabrechnungService.berechneUndSpeichere(buchung);
        }

        buchung.setStatus(neuerStatus);

        return toDTO(buchungRepository.save(buchung));
    }

    private boolean istStatusuebergangErlaubt(
            Buchungsstatus alterStatus,
            Buchungsstatus neuerStatus
    ) {
        if (alterStatus == null || neuerStatus == alterStatus) {
            return false;
        }

        return switch (alterStatus) {
            case ANFRAGE ->
                    neuerStatus == Buchungsstatus.RESERVIERT
                            || neuerStatus == Buchungsstatus.BESTAETIGT
                            || neuerStatus == Buchungsstatus.ABGELEHNT;

            case RESERVIERT ->
                    neuerStatus == Buchungsstatus.BESTAETIGT
                            || neuerStatus == Buchungsstatus.STORNIERT;

            case BESTAETIGT ->
                    neuerStatus == Buchungsstatus.ABRECHENBAR
                            || neuerStatus == Buchungsstatus.STORNIERT;

            case ABRECHENBAR ->
                    neuerStatus == Buchungsstatus.ABGESCHLOSSEN
                            || neuerStatus == Buchungsstatus.STORNIERT;

            case ABGESCHLOSSEN ->
                    neuerStatus == Buchungsstatus.ABRECHENBAR;

            case ABGELEHNT, STORNIERT -> false;
        };
    }

    private void synchronisiereBuchungspositionen(
            Buchung buchung,
            BuchungDTO dto,
            Mietobjekt mietobjekt,
            Buchungsquelle buchungsquelle
    ) {
        List<BuchungMietbereichDTO> positionen = dto.getPositionen();

        // Abwärtskompatibilität für DTOs, die noch mietbereichIds verwenden.
        if (positionen == null) {
            positionen = new ArrayList<>();

            if (dto.getMietbereichIds() != null) {
                for (Long mietbereichId : dto.getMietbereichIds()) {
                    BuchungMietbereichDTO position =
                            new BuchungMietbereichDTO();

                    position.setMietbereichId(mietbereichId);
                    position.setAnzahl(1);

                    positionen.add(position);
                }
            }
        }

        Map<Long, BuchungMietbereich> bestehendePositionen =
                new HashMap<>();

        for (BuchungMietbereich bestehend : buchung.getMietbereiche()) {
            bestehendePositionen.put(
                    bestehend.getMietbereich().getId(),
                    bestehend
            );
        }

        Set<Long> gewuenschteMietbereichIds = new HashSet<>();

        for (BuchungMietbereichDTO position : positionen) {

            if (position.getMietbereichId() == null) {
                throw new IllegalArgumentException(
                        ErrorMessages.BUCHUNG_POSITION_MIETBEREICH_REQUIRED
                );
            }

            if (position.getAnzahl() == null || position.getAnzahl() < 1) {
                throw new IllegalArgumentException(
                        ErrorMessages.BUCHUNG_POSITION_ANZAHL_INVALID
                );
            }

            Long mietbereichId = position.getMietbereichId();

            if (!gewuenschteMietbereichIds.add(mietbereichId)) {
                throw new IllegalArgumentException(
                        "Ein Mietbereich darf einer Buchung nur einmal "
                                + "zugeordnet werden: " + mietbereichId
                );
            }

            Mietbereich mietbereich = getMietbereiche(
                    List.of(mietbereichId),
                    mietobjekt
            ).getFirst();

            pruefeBuchungsquelle(
                    buchungsquelle,
                    mietobjekt,
                    mietbereich
            );

            BuchungMietbereich zuordnung =
                    bestehendePositionen.get(mietbereichId);

            if (zuordnung != null) {
                // Bestehende Position: Snapshot nicht verändern.
                zuordnung.setAnzahl(position.getAnzahl());
                continue;
            }

            // Neue Position: gültigen Preis ermitteln.
            Mietpreis mietpreis = ermittleMietpreis(
                    buchung,
                    mietbereich,
                    buchungsquelle
            );

            zuordnung = new BuchungMietbereich();

            zuordnung.setBuchung(buchung);
            zuordnung.setMietbereich(mietbereich);
            zuordnung.setAnzahl(position.getAnzahl());

            zuordnung.setVereinbarterPreis(mietpreis.getPreis());
            zuordnung.setBerechnungsart(mietbereich.getBerechnungsart());

            buchung.getMietbereiche().add(zuordnung);
        }

        // Nicht mehr gewünschte Positionen entfernen.
        buchung.getMietbereiche().removeIf(
                position -> !gewuenschteMietbereichIds.contains(
                        position.getMietbereich().getId()
                )
        );
    }

    private void berechneBuchungsrabatt(
            Buchung buchung,
            BuchungDTO dto,
            boolean rabattAuswahlUnveraendert
    ) {
        Rabattart rabattart = dto.getRabattart() == null
                ? Rabattart.KEIN_RABATT
                : dto.getRabattart();
        RabattwertTyp rabattwertTyp = dto.getRabattwertTyp();
        BigDecimal rabattwert = dto.getRabattwert();

        if (rabattart == Rabattart.INDIVIDUELL) {
            if (rabattwertTyp == null || rabattwert == null || rabattwert.signum() < 0) {
                throw new IllegalArgumentException(
                        "Für einen individuellen Rabatt müssen Werttyp und ein nicht negativer Betrag angegeben werden."
                );
            }
            if (rabattwertTyp == RabattwertTyp.PROZENTUAL
                    && rabattwert.compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException(
                        "Ein prozentualer Rabatt darf maximal 100 % betragen."
                );
            }
        } else {
            rabattwertTyp = null;
            rabattwert = null;
        }

        buchung.setRabattart(rabattart);
        buchung.setRabattwertTyp(rabattwertTyp);
        buchung.setRabattwert(rabattwert);

        BigDecimal gesamtMietpreis = buchung.getMietbereiche().stream()
                .map(this::berechnePositionspreis)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal gesamtrabatt = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        if (rabattart == Rabattart.INDIVIDUELL
                && rabattwertTyp == RabattwertTyp.ABSOLUT) {
            gesamtrabatt = rabattwert.min(gesamtMietpreis).setScale(2, RoundingMode.HALF_UP);
            buchung.getMietbereiche().forEach(position -> {
                position.setRabattwertTyp(null);
                position.setRabattwert(null);
                position.setRabattbetrag(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            });
            buchung.setRabattbetrag(gesamtrabatt);
            return;
        }

        for (BuchungMietbereich position : buchung.getMietbereiche()) {
            RabattwertTyp positionsRabattwertTyp = null;
            BigDecimal positionsRabattwert = null;

            boolean bestehenderSnapshot = rabattAuswahlUnveraendert
                    && position.getId() != null;

            if (bestehenderSnapshot) {
                positionsRabattwertTyp = position.getRabattwertTyp();
                positionsRabattwert = position.getRabattwert();
            } else if (rabattart == Rabattart.VEREIN
                    || rabattart == Rabattart.VERBAND) {
                MietbereichRabatt regel = mietbereichRabattRepository
                        .findByMietbereichIdAndRabattartAndAktivTrue(
                                position.getMietbereich().getId(),
                                rabattart
                        )
                        .orElse(null);
                if (regel != null) {
                    positionsRabattwertTyp = regel.getRabattwertTyp();
                    positionsRabattwert = regel.getRabattwert();
                }
            } else if (rabattart == Rabattart.INDIVIDUELL
                    && rabattwertTyp == RabattwertTyp.PROZENTUAL) {
                boolean rabattierbar = !mietbereichRabattRepository
                        .findByMietbereichIdAndAktivTrue(
                                position.getMietbereich().getId()
                        )
                        .isEmpty();
                if (rabattierbar) {
                    positionsRabattwertTyp = rabattwertTyp;
                    positionsRabattwert = rabattwert;
                }
            }

            BigDecimal positionspreis = berechnePositionspreis(position);
            BigDecimal positionsrabatt = berechnePositionsrabatt(
                    positionspreis,
                    positionsRabattwertTyp,
                    positionsRabattwert
            );

            position.setRabattwertTyp(positionsRabattwertTyp);
            position.setRabattwert(positionsRabattwert);
            position.setRabattbetrag(positionsrabatt);
            gesamtrabatt = gesamtrabatt.add(positionsrabatt);
        }

        buchung.setRabattbetrag(gesamtrabatt.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal berechnePositionspreis(BuchungMietbereich position) {
        if (position.getVereinbarterPreis() == null || position.getAnzahl() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return position.getVereinbarterPreis()
                .multiply(BigDecimal.valueOf(position.getAnzahl()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validiereMietbereichVoraussetzungen(Buchung buchung) {
        Set<Long> ausgewaehlteIds = buchung.getMietbereiche().stream()
                .map(position -> position.getMietbereich().getId())
                .collect(java.util.stream.Collectors.toSet());

        for (BuchungMietbereich position : buchung.getMietbereiche()) {
            Mietbereich bereich = position.getMietbereich();
            for (Mietbereich voraussetzung : bereich.getVoraussetzungen()) {
                if (!ausgewaehlteIds.contains(voraussetzung.getId())) {
                    throw new IllegalArgumentException(
                            "Der Mietbereich '" + bereich.getBezeichnung()
                                    + "' kann nur zusammen mit '"
                                    + voraussetzung.getBezeichnung() + "' gebucht werden."
                    );
                }
            }
        }
    }

    private BigDecimal berechnePositionsrabatt(
            BigDecimal positionspreis,
            RabattwertTyp rabattwertTyp,
            BigDecimal rabattwert
    ) {
        if (rabattwertTyp == null || rabattwert == null || positionspreis.signum() <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal rabatt = rabattwertTyp == RabattwertTyp.PROZENTUAL
                ? positionspreis.multiply(rabattwert)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                : rabattwert.min(positionspreis);

        return rabatt.min(positionspreis).setScale(2, RoundingMode.HALF_UP);
    }

    private void setzeBuchungspositionen(
            Buchung buchung,
            BuchungDTO dto,
            Mietobjekt mietobjekt,
            Buchungsquelle buchungsquelle
    ) {
        List<BuchungMietbereichDTO> positionen = dto.getPositionen();

        if (positionen == null) {
            // Abwärtskompatibilität: bisherige mietbereichIds verwenden.
            List<Mietbereich> mietbereiche =
                    getMietbereiche(dto.getMietbereichIds(), mietobjekt);

            for (Mietbereich mietbereich : mietbereiche) {
                pruefeBuchungsquelle(
                        buchungsquelle,
                        mietobjekt,
                        mietbereich
                );
                BuchungMietbereich zuordnung = new BuchungMietbereich();
                zuordnung.setBuchung(buchung);
                zuordnung.setMietbereich(mietbereich);
                zuordnung.setAnzahl(1);
                buchung.getMietbereiche().add(zuordnung);
            }

            return;
        }

        for (BuchungMietbereichDTO position : positionen) {
            if (position.getMietbereichId() == null) {
                throw new IllegalArgumentException(
                        ErrorMessages.BUCHUNG_POSITION_MIETBEREICH_REQUIRED
                );
            }

            if (position.getAnzahl() == null || position.getAnzahl() < 1) {
                throw new IllegalArgumentException(
                        ErrorMessages.BUCHUNG_POSITION_ANZAHL_INVALID
                );
            }

            Mietbereich mietbereich = getMietbereiche(
                    List.of(position.getMietbereichId()),
                    mietobjekt
            ).getFirst();

            BuchungMietbereich zuordnung = new BuchungMietbereich();
            buchung.getMietbereiche().add(zuordnung);
            pruefeBuchungsquelle(
                    buchungsquelle,
                    mietobjekt,
                    mietbereich
            );
            zuordnung.setBuchung(buchung);
            zuordnung.setMietbereich(mietbereich);
            zuordnung.setAnzahl(position.getAnzahl());
        }
    }

    private void pruefeBuchungsquelle(
            Buchungsquelle buchungsquelle,
            Mietobjekt mietobjekt,
            Mietbereich mietbereich
    ) {
        if (buchungsquelle == null) {
            throw new IllegalArgumentException(
                    ErrorMessages.BUCHUNG_QUELLE_REQUIRED
            );
        }

        boolean amMietobjektAktiv = switch (buchungsquelle) {
            case DIREKT -> mietobjekt.isDirektbuchungAktiv();
            case AIRBNB -> mietobjekt.isAirbnbAktiv();
        };

        if (!amMietobjektAktiv) {
            throw new IllegalArgumentException(
                    String.format(
                            java.util.Locale.GERMAN,
                            ErrorMessages.BUCHUNG_QUELLE_MIETOBJEKT_NOT_ENABLED,
                            buchungsquelle
                    )
            );
        }

        boolean amMietbereichAktiv = switch (buchungsquelle) {
            case DIREKT -> mietbereich.isDirektbuchungAktiv();
            case AIRBNB -> mietbereich.isAirbnbAktiv();
        };

        if (!amMietbereichAktiv) {
            throw new IllegalArgumentException(
                    String.format(
                            java.util.Locale.GERMAN,
                            ErrorMessages.BUCHUNG_QUELLE_MIETBEREICH_NOT_ENABLED,
                            buchungsquelle,
                            mietbereich.getBezeichnung()
                    )
            );
        }
    }

    private void pruefeBestand(
            Buchung buchung,
            Long eigeneBuchungId
    ) {
        List<Buchungsstatus> bestandsrelevanteStatus = List.of(
                Buchungsstatus.RESERVIERT,
                Buchungsstatus.BESTAETIGT
        );

        List<Buchung> bestehendeBuchungen;

        if (eigeneBuchungId == null) {
            bestehendeBuchungen =
                    buchungRepository
                            .findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(
                                    bestandsrelevanteStatus,
                                    buchung.getAbreise(),
                                    buchung.getAnreise()
                            );
        } else {
            bestehendeBuchungen =
                    buchungRepository
                            .findByStatusInAndAnreiseLessThanAndAbreiseGreaterThanAndIdNot(
                                    bestandsrelevanteStatus,
                                    buchung.getAbreise(),
                                    buchung.getAnreise(),
                                    eigeneBuchungId
                            );
        }

        for (BuchungMietbereich neuePosition : buchung.getMietbereiche()) {
            Mietbereich mietbereich = neuePosition.getMietbereich();

            int bereitsGebucht = bestehendeBuchungen.stream()
                    .filter(b -> b.getMietobjekt().getId()
                            .equals(buchung.getMietobjekt().getId()))
                    .flatMap(b -> b.getMietbereiche().stream())
                    .filter(position -> position.getMietbereich().getId()
                            .equals(mietbereich.getId()))
                    .mapToInt(BuchungMietbereich::getAnzahl)
                    .sum();

            int angefordert = neuePosition.getAnzahl();
            int bestand = mietbereich.getBestand();

            if (bereitsGebucht + angefordert > bestand) {
                throw new IllegalArgumentException(
                        String.format(
                                Locale.GERMAN,
                                ErrorMessages.BUCHUNG_BESTAND_UNZUREICHEND,
                                mietbereich.getBezeichnung(),
                                bestand,
                                bereitsGebucht,
                                angefordert
                        )
                );
            }
        }
    }

    private Mietpreis ermittleMietpreis(
            Buchung buchung,
            Mietbereich mietbereich,
            Buchungsquelle buchungsquelle
    ) {
        if (buchung.getCreatedAt() == null) {
            throw new IllegalStateException(
                    "Das Erstellungsdatum der Buchung ist noch nicht gesetzt."
            );
        }

        LocalDate stichtag = buchung.getCreatedAt()
                .atZone(ZoneId.of("Europe/Berlin"))
                .toLocalDate();

        return mietpreisRepository
                .findFirstByMietbereichIdAndBuchungsquelleAndGueltigAbLessThanEqualOrderByGueltigAbDesc(
                        mietbereich.getId(),
                        buchungsquelle,
                        stichtag
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Kein gültiger Mietpreis für den Mietbereich '"
                                        + mietbereich.getBezeichnung()
                                        + "' am "
                                        + stichtag
                                        + " für die Buchungsquelle "
                                        + buchungsquelle
                                        + " vorhanden."
                        )
                );
    }
}
