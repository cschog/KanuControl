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
import com.kcserver.vermietung.enumtype.Finanzbewegungsart;
import com.kcserver.vermietung.enumtype.Finanzkategorie;
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
    private final HomepageStatusMailService homepageStatusMailService;
    private final MieterRepository mieterRepository;
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
    private final BuchungFinanzbewegungRepository
            buchungFinanzbewegungRepository;

    private static final LocalDateTime UNBEFRISTET_BIS =
            LocalDateTime.of(3000, 12, 31, 14, 0);

    public BuchungService(
            BuchungRepository buchungRepository,
            MieterRepository mieterRepository,
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
            BuchungVerbrauchskostenRepository buchungVerbrauchskostenRepository,
            BuchungFinanzbewegungRepository buchungFinanzbewegungRepository,
            HomepageStatusMailService homepageStatusMailService
    ) {
        this.buchungRepository = buchungRepository;
        this.homepageStatusMailService = homepageStatusMailService;
        this.mieterRepository = mieterRepository;
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
        this.buchungFinanzbewegungRepository =
                buchungFinanzbewegungRepository;
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

        Buchung buchung = buchungRepository.findLockedById(id)
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

            homepageStatusMailService.vormerken(buchung, Buchungsstatus.ABRECHENBAR);
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

    /** Gemeinsame Objektsperre für Sperrzeiten und neue Homepage-Anfragen. */
    public void sperreMietobjekt(Long id) {
        mietobjektRepository.findLockedById(id);
    }

    public BuchungDTO create(BuchungDTO dto) {

        if (!dto.isTeilnehmerdatenGueltig()) {
            throw new IllegalArgumentException("Bitte Teilnehmerzahlen und Altersspanne der Jugendlichen prüfen.");
        }

        Person mieter = getMieter(dto.getMieterId());

        Mietobjekt mietobjekt =
                getMietobjekt(dto.getMietobjektId());
        mietobjektRepository.findLockedById(mietobjekt.getId());

        Verein veranstalter =
                getVeranstalter(dto.getVeranstalterVereinId());

        Buchung buchung = new Buchung();

        buchung.setBuchungsquelle(
                dto.getBuchungsquelle() != null
                        ? dto.getBuchungsquelle()
                        : Buchungsquelle.DIREKT
        );

        buchung.setMieter(mieter);
        ensureMieter(mieter);
        buchung.setKautionBetrag(buchung.getBuchungsquelle() == Buchungsquelle.DIREKT
                ? Optional.ofNullable(dto.getKautionBetrag()).orElse(BigDecimal.ZERO)
                : BigDecimal.ZERO);
        buchung.setAnzahlungBetrag(buchung.getBuchungsquelle() == Buchungsquelle.DIREKT
                ? Optional.ofNullable(dto.getAnzahlungBetrag()).orElse(BigDecimal.ZERO)
                : BigDecimal.ZERO);

        buchung.setMietobjekt(mietobjekt);
        buchung.setVeranstalter(veranstalter);
        buchung.setBemerkung(dto.getBemerkung());
        buchung.setBewertung(dto.getBewertung());
        buchung.setAnzahlJugendliche(dto.getAnzahlJugendliche());
        buchung.setJugendAlterVon(dto.getJugendAlterVon());
        buchung.setJugendAlterBis(dto.getJugendAlterBis());
        buchung.setAnzahlErwachsene(dto.getAnzahlErwachsene());

        boolean unbefristet = dto.isUnbefristet();
        LocalDateTime anreise = dto.getAnreise();
        LocalDateTime abreise = unbefristet
                ? UNBEFRISTET_BIS
                : dto.getAbreise();

        pruefeBuchungszeiten(mietobjekt, anreise, abreise, unbefristet);

        buchung.setAnreise(anreise);
        buchung.setAbreise(abreise);
        SperrzeitService.pruefe(mietobjekt, anreise, abreise);

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

        if (!dto.isTeilnehmerdatenGueltig()) {
            throw new IllegalArgumentException("Bitte Teilnehmerzahlen und Altersspanne der Jugendlichen prüfen.");
        }

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
        mietobjektRepository.findLockedById(mietobjekt.getId());

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
        ensureMieter(mieter);
        BigDecimal neueKaution = buchung.getBuchungsquelle() == Buchungsquelle.DIREKT
                ? Optional.ofNullable(dto.getKautionBetrag()).orElse(BigDecimal.ZERO)
                : BigDecimal.ZERO;
        BigDecimal bereitsVerwahrteKaution =
                buchungFinanzbewegungRepository
                        .findByBuchungIdOrderByDatumAscIdAsc(buchung.getId())
                        .stream()
                        .filter(bewegung -> bewegung.getKategorie()
                                == Finanzkategorie.KAUTION)
                        .map(bewegung -> switch (bewegung.getArt()) {
                            case EINGANG -> bewegung.getBetrag();
                            case RUECKZAHLUNG, VERRECHNUNG -> bewegung.getBetrag().negate();
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .max(BigDecimal.ZERO);
        if (neueKaution.compareTo(bereitsVerwahrteKaution) < 0) {
            throw new IllegalArgumentException(
                    "Der Kautionsbetrag kann nicht unter den noch verwahrten "
                            + "Kautionssaldo von " + bereitsVerwahrteKaution
                            + " € reduziert werden. Bitte erst zurückzahlen "
                            + "oder verrechnen."
            );
        }
        buchung.setKautionBetrag(neueKaution);
        buchung.setAnzahlungBetrag(buchung.getBuchungsquelle() == Buchungsquelle.DIREKT
                ? Optional.ofNullable(dto.getAnzahlungBetrag()).orElse(BigDecimal.ZERO)
                : BigDecimal.ZERO);
        buchung.setMietobjekt(mietobjekt);
        buchung.setVeranstalter(veranstalter);

        buchung.setBemerkung(dto.getBemerkung());
        buchung.setBewertung(dto.getBewertung());
        buchung.setAnzahlJugendliche(dto.getAnzahlJugendliche());
        buchung.setJugendAlterVon(dto.getJugendAlterVon());
        buchung.setJugendAlterBis(dto.getJugendAlterBis());
        buchung.setAnzahlErwachsene(dto.getAnzahlErwachsene());

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
        SperrzeitService.pruefe(mietobjekt, anreise, abreise);

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

        if (!abreise.toLocalDate().isAfter(anreise.toLocalDate())) {
            throw new IllegalArgumentException(
                    "Der Mindestaufenthalt beträgt eine Übernachtung. Die Abreise muss frühestens am Folgetag liegen."
            );
        }
    }

    private boolean sindBuchungsdatenUnveraendert(
            Buchung buchung,
            BuchungDTO dto
    ) {
        if (dto.getMieterId() == null
                || !Objects.equals(buchung.getAnzahlJugendliche(), dto.getAnzahlJugendliche())
                || !Objects.equals(buchung.getJugendAlterVon(), dto.getJugendAlterVon())
                || !Objects.equals(buchung.getJugendAlterBis(), dto.getJugendAlterBis())
                || !Objects.equals(buchung.getAnzahlErwachsene(), dto.getAnzahlErwachsene())
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
     * Vergibt die nächste Buchungsnummer für das Jahr der Anreise.
     */
    private void vergebeBuchungsnummer(Buchung buchung) {

        int jahr = buchung.getAnreise().getYear();

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

        int letzteVorhandeneNummer = buchungRepository
                .findTopByBuchungsjahrOrderByLaufendeNummerDesc(jahr)
                .map(Buchung::getLaufendeNummer).orElse(0);
        int naechsteNummer = Math.max(counter.getLetzteNummer(), letzteVorhandeneNummer) + 1;

        counter.setLetzteNummer(naechsteNummer);

        buchungsnummerCounterRepository.save(counter);

        buchung.setBuchungsjahr(jahr);
        buchung.setLaufendeNummer(naechsteNummer);
    }

    private Person getMieter(Long mieterId) {
        // Mit dem Löschen eines Mieters serialisieren, bevor ein Profil angelegt
        // oder die neue Buchungsverknüpfung gespeichert wird.
        Person person = personRepository.findLockedById(mieterId)
                .orElseThrow(() -> new IllegalArgumentException("Mieter nicht gefunden: " + mieterId));
        if (!person.isKjfpSichtbar() && !mieterRepository.existsByPersonId(mieterId)) {
            throw new IllegalArgumentException("Der ausgewählte Mieter ist nicht mehr vorhanden. Bitte die Mieterauswahl neu laden.");
        }
        return person;
    }

    private void ensureMieter(Person person) {
        Mieter mieter = mieterRepository.findByPersonId(person.getId()).orElseGet(Mieter::new);
        mieter.setPerson(person);
        mieter.setAktiv(true);
        mieterRepository.save(mieter);
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
        dto.setRabattbetrag(BuchungPreiskalkulation.rabatt(buchung));
        dto.setKautionBetrag(buchung.getKautionBetrag());
        dto.setAnzahlungBetrag(buchung.getAnzahlungBetrag());

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
                            position.setRabattbetrag(BuchungPreiskalkulation.positionsRabatt(
                                    zuordnung, zuordnung.getRabattwertTyp(), zuordnung.getRabattwert()));

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
        dto.setHerkunft(buchung.getHerkunft());
        dto.setBewertung(buchung.getBewertung());
        dto.setAnzahlJugendliche(buchung.getAnzahlJugendliche());
        dto.setJugendAlterVon(buchung.getJugendAlterVon());
        dto.setJugendAlterBis(buchung.getJugendAlterBis());
        dto.setAnzahlErwachsene(buchung.getAnzahlErwachsene());


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

            homepageStatusMailService.vormerken(buchung, Buchungsstatus.ABRECHENBAR);
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
        Buchung buchung = buchungRepository.findLockedById(id)
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
            mietobjektRepository.findLockedById(buchung.getMietobjekt().getId());
            pruefeBestand(buchung, buchung.getId());
        }

        /*
         * Die Verbrauchsabrechnung muss erfolgreich sein,
         * bevor der Status geändert wird.
         */
        if (neuerStatus == Buchungsstatus.ABGESCHLOSSEN) {
            pruefeKautionsabschluss(buchung);
            verbrauchsabrechnungService.berechneUndSpeichere(buchung);
        }

        homepageStatusMailService.vormerken(buchung, neuerStatus);

        buchung.setStatus(neuerStatus);

        return toDTO(buchungRepository.save(buchung));
    }

    private void pruefeKautionsabschluss(Buchung buchung) {
        if (buchung.getBuchungsquelle() != Buchungsquelle.DIREKT) {
            return;
        }

        List<BuchungFinanzbewegung> kautionsbewegungen =
                buchungFinanzbewegungRepository
                        .findByBuchungIdOrderByDatumAscIdAsc(buchung.getId())
                        .stream()
                        .filter(bewegung ->
                                bewegung.getKategorie()
                                        == Finanzkategorie.KAUTION)
                        .toList();
        BigDecimal kautionSoll = Optional.ofNullable(
                buchung.getKautionBetrag()
        ).orElse(BigDecimal.ZERO);
        if (kautionSoll.signum() <= 0 && kautionsbewegungen.isEmpty()) {
            return;
        }

        BigDecimal eingegangen = summeFinanzbewegungen(
                kautionsbewegungen,
                Finanzbewegungsart.EINGANG
        );
        BigDecimal zurueckgezahlt = summeFinanzbewegungen(
                kautionsbewegungen,
                Finanzbewegungsart.RUECKZAHLUNG
        );
        BigDecimal verrechnet = summeFinanzbewegungen(
                kautionsbewegungen,
                Finanzbewegungsart.VERRECHNUNG
        );

        List<BuchungFinanzbewegung> alleBewegungen =
                buchungFinanzbewegungRepository
                        .findByBuchungIdOrderByDatumAscIdAsc(buchung.getId());
        BigDecimal mietEingegangen = alleBewegungen.stream()
                .filter(bewegung -> bewegung.getKategorie() == Finanzkategorie.ANZAHLUNG
                        || bewegung.getKategorie() == Finanzkategorie.MIETE)
                .filter(bewegung -> bewegung.getArt() == Finanzbewegungsart.EINGANG)
                .map(BuchungFinanzbewegung::getBetrag)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal mietRueckzahlungen = alleBewegungen.stream()
                .filter(bewegung -> bewegung.getKategorie() == Finanzkategorie.ANZAHLUNG
                        || bewegung.getKategorie() == Finanzkategorie.MIETE)
                .filter(bewegung -> bewegung.getArt() == Finanzbewegungsart.RUECKZAHLUNG)
                .map(BuchungFinanzbewegung::getBetrag)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal mietBrutto = buchung.getMietbereiche().stream()
                .map(this::berechnePositionspreis)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal mietSoll = mietBrutto.subtract(BuchungPreiskalkulation.rabatt(buchung)).max(BigDecimal.ZERO);
        BigDecimal mietUeberzahlung = mietEingegangen.subtract(mietSoll)
                .max(BigDecimal.ZERO);
        BigDecimal mietRueckzahlungAusKaution = mietRueckzahlungen
                .subtract(mietUeberzahlung)
                .max(BigDecimal.ZERO)
                .min(eingegangen.subtract(zurueckgezahlt).subtract(verrechnet).max(BigDecimal.ZERO));

        BigDecimal nochOffen = kautionSoll.subtract(eingegangen);
        if (nochOffen.signum() > 0) {
            throw new IllegalArgumentException(
                    "Die Buchung kann erst abgeschlossen werden, wenn die "
                            + "Kaution vollständig eingegangen ist. Noch offen: "
                            + nochOffen + " €."
            );
        }

        BigDecimal kautionssaldo = eingegangen
                .subtract(zurueckgezahlt)
                .subtract(verrechnet)
                .subtract(mietRueckzahlungAusKaution);
        if (kautionssaldo.signum() != 0) {
            throw new IllegalArgumentException(
                    "Die Buchung kann erst abgeschlossen werden, wenn die "
                            + "Kaution vollständig zurückgezahlt oder mit "
                            + "Verbrauchskosten verrechnet wurde. Noch nicht "
                            + "abgerechnet: " + kautionssaldo + " €."
            );
        }
    }

    private BigDecimal summeFinanzbewegungen(
            List<BuchungFinanzbewegung> bewegungen,
            Finanzbewegungsart art
    ) {
        return bewegungen.stream()
                .filter(bewegung -> bewegung.getArt() == art)
                .map(BuchungFinanzbewegung::getBetrag)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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

        if (buchung.getBuchungsquelle() == Buchungsquelle.AIRBNB) {
            rabattart = Rabattart.KEIN_RABATT;
            rabattwertTyp = null;
            rabattwert = null;
            rabattAuswahlUnveraendert = false;
        }

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

            BigDecimal positionsrabatt = BuchungPreiskalkulation.positionsRabatt(
                    position, positionsRabattwertTyp, positionsRabattwert);

            position.setRabattwertTyp(positionsRabattwertTyp);
            position.setRabattwert(positionsRabattwert);
            position.setRabattbetrag(positionsrabatt);
            gesamtrabatt = gesamtrabatt.add(positionsrabatt);
        }

        buchung.setRabattbetrag(gesamtrabatt.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal berechnePositionspreis(BuchungMietbereich position) {
        return BuchungPreiskalkulation.preis(position);
    }

    private void validiereMietbereichVoraussetzungen(Buchung buchung) {
        Set<Long> ausgewaehlteIds = buchung.getMietbereiche().stream()
                .map(position -> position.getMietbereich().getId())
                .collect(java.util.stream.Collectors.toSet());

        for (BuchungMietbereich position : buchung.getMietbereiche()) {
            Mietbereich bereich = position.getMietbereich();
            Set<Mietbereich> voraussetzungen = buchung.getBuchungsquelle() == Buchungsquelle.DIREKT
                    ? bereich.getVoraussetzungenDirekt()
                    : bereich.getVoraussetzungenAirbnb();
            for (Mietbereich voraussetzung : voraussetzungen) {
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

    /** Nutzt dieselbe Zeit-, Bereichs- und Bestandsprüfung wie die interne Reservierung. */
    @Transactional(readOnly = true, noRollbackFor = IllegalArgumentException.class)
    public void pruefeHomepageVerfuegbarkeit(Long mietobjektId, LocalDate anreise, LocalDate abreise,
            List<BuchungMietbereichDTO> positionen) {
        Mietobjekt objekt = getMietobjekt(mietobjektId);
        if (!objekt.isMietbar() || !objekt.isDirektbuchungAktiv()
                || objekt.getAnreisezeit() == null || objekt.getAbreisezeit() == null) {
            throw new IllegalArgumentException("Das Mietobjekt ist online nicht buchbar.");
        }
        if (anreise == null || abreise == null || anreise.isBefore(LocalDate.now(ZoneId.of("Europe/Berlin")))
                || !abreise.isAfter(anreise) || positionen == null || positionen.isEmpty()) {
            throw new IllegalArgumentException("Bitte einen zukünftigen Zeitraum mit mindestens einer Übernachtung und Mietbereiche auswählen.");
        }
        Buchung pruefung = new Buchung();
        pruefung.setMietobjekt(objekt);
        pruefung.setBuchungsquelle(Buchungsquelle.DIREKT);
        pruefung.setAnreise(anreise.atTime(objekt.getAnreisezeit()));
        pruefung.setAbreise(abreise.atTime(objekt.getAbreisezeit()));
        pruefeBuchungszeiten(objekt, pruefung.getAnreise(), pruefung.getAbreise(), false);
        if (positionen.stream().map(BuchungMietbereichDTO::getMietbereichId).distinct().count() != positionen.size()) {
            throw new IllegalArgumentException("Mietbereiche dürfen nur einmal ausgewählt werden.");
        }
        BuchungDTO dto = new BuchungDTO(); dto.setPositionen(positionen);
        setzeBuchungspositionen(pruefung, dto, objekt, Buchungsquelle.DIREKT);
        validiereMietbereichVoraussetzungen(pruefung);
        pruefeBestand(pruefung, null);
    }

    private void pruefeBestand(
            Buchung buchung,
            Long eigeneBuchungId
    ) {
        SperrzeitService.pruefe(buchung.getMietobjekt(), buchung.getAnreise(), buchung.getAbreise());
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

        Map<Long, Mietbereich> beanspruchteBereiche = new HashMap<>();
        Map<Long, Integer> angeforderteMengen = new HashMap<>();
        Set<Long> bereitsInnerhalbDerBuchungBelegt = new HashSet<>();

        for (BuchungMietbereich position : buchung.getMietbereiche()) {
            Set<Mietbereich> ressourcen = ermittleBelegteRessourcen(
                    position.getMietbereich(), new HashSet<>()
            );
            for (Mietbereich ressource : ressourcen) {
                if (!bereitsInnerhalbDerBuchungBelegt.add(ressource.getId())) {
                    throw new IllegalArgumentException(
                            "Die ausgewählten Mietbereiche überschneiden sich bei '"
                                    + ressource.getBezeichnung()
                                    + "' und können nicht gemeinsam gebucht werden."
                    );
                }
                beanspruchteBereiche.put(ressource.getId(), ressource);
                angeforderteMengen.merge(ressource.getId(), position.getAnzahl(), Integer::sum);
            }
        }

        for (Map.Entry<Long, Mietbereich> entry : beanspruchteBereiche.entrySet()) {
            Mietbereich ressource = entry.getValue();
            int bereitsGebucht = bestehendeBuchungen.stream()
                    .filter(b -> b.getMietobjekt().getId()
                            .equals(buchung.getMietobjekt().getId()))
                    .flatMap(b -> b.getMietbereiche().stream())
                    .filter(position -> ermittleBelegteRessourcen(
                            position.getMietbereich(), new HashSet<>()
                    ).stream().anyMatch(belegt -> belegt.getId().equals(entry.getKey())))
                    .mapToInt(position -> position.getAnzahl())
                    .sum();
            int angefordert = angeforderteMengen.get(entry.getKey());
            int bestand = ressource.getBestand();

            if (bereitsGebucht + angefordert > bestand) {
                throw new IllegalArgumentException(
                        String.format(
                                Locale.GERMAN,
                                ErrorMessages.BUCHUNG_BESTAND_UNZUREICHEND,
                                ressource.getBezeichnung(),
                                bestand,
                                bereitsGebucht,
                                angefordert
                        )
                );
            }
        }
    }

    /** Eine Abfrage pro Monat; freigegebener Mietername, aber keine Kontaktdaten oder internen Buchungs-IDs. */
    @Transactional(readOnly = true)
    public com.kcserver.vermietung.dto.HomepageIntegrationDTO.Kalender homepageKalender(
            Mietobjekt objekt, Mietbereich bereich, java.time.YearMonth monat, Set<Long> freigegeben) {
        Set<Mietbereich> auswahl = new HashSet<>();
        kalenderVoraussetzungen(bereich, freigegeben, auswahl);
        Map<Long, Mietbereich> ressourcen = new HashMap<>();
        boolean ueberschneidung = false;
        for (Mietbereich b : auswahl) {
            for (Mietbereich r : ermittleBelegteRessourcen(b, new HashSet<>())) {
                if (ressourcen.putIfAbsent(r.getId(), r) != null) ueberschneidung = true;
            }
        }
        List<Buchung> buchungen = buchungRepository.findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(
                List.of(Buchungsstatus.RESERVIERT, Buchungsstatus.BESTAETIGT, Buchungsstatus.ABRECHENBAR, Buchungsstatus.ABGESCHLOSSEN),
                monat.plusMonths(1).atDay(1).atTime(objekt.getAbreisezeit()),
                monat.atDay(1).atStartOfDay()).stream()
                .filter(b -> Objects.equals(b.getMietobjekt().getId(), objekt.getId())).toList();
        // Zusammensetzungen nur einmal auflösen, nicht für jeden Kalendertag erneut.
        record Belegung(Buchung buchung, Map<Long, Integer> mengen) {}
        List<Belegung> belegungen = new ArrayList<>();
        for (Buchung b : buchungen) {
            Map<Long, Integer> mengen = new HashMap<>();
            for (BuchungMietbereich p : b.getMietbereiche()) {
                for (Mietbereich r : ermittleBelegteRessourcen(p.getMietbereich(), new HashSet<>())) {
                    if (ressourcen.containsKey(r.getId())) mengen.merge(r.getId(), p.getAnzahl(), Integer::sum);
                }
            }
            if (!mengen.isEmpty()) belegungen.add(new Belegung(b, mengen));
        }
        List<com.kcserver.vermietung.dto.HomepageIntegrationDTO.KalenderTag> tage = new ArrayList<>();
        for (int d = 1; d <= monat.lengthOfMonth(); d++) {
            LocalDate datum = monat.atDay(d);
            List<com.kcserver.vermietung.dto.HomepageIntegrationDTO.KalenderBelegung> zeitbelegungen = belegungen.stream()
                    .map(Belegung::buchung)
                    .filter(b -> b.getAnreise().isBefore(datum.plusDays(1).atStartOfDay())
                            && b.getAbreise().isAfter(datum.atStartOfDay()))
                    .sorted(java.util.Comparator.comparing(Buchung::getAnreise))
                    .map(b -> new com.kcserver.vermietung.dto.HomepageIntegrationDTO.KalenderBelegung(
                            b.getAnreise().isBefore(datum.atStartOfDay()) ? datum.atStartOfDay() : b.getAnreise(),
                            b.getAbreise().isAfter(datum.plusDays(1).atStartOfDay()) ? datum.plusDays(1).atStartOfDay() : b.getAbreise(),
                            b.getStatus() == Buchungsstatus.RESERVIERT ? "RESERVIERT" : "BELEGT", b.getBuchungsnummer(), homepageMietername(b)))
                    .toList();
            LocalDateTime von = datum.atTime(objekt.getAnreisezeit());
            LocalDateTime bis = datum.plusDays(1).atTime(objekt.getAbreisezeit());
            Map<Long, Integer> alle = new HashMap<>(), feste = new HashMap<>();
            Set<String> nummern = new TreeSet<>();
            for (Belegung belegung : belegungen) {
                Buchung b = belegung.buchung();
                if (!b.getAnreise().isBefore(bis) || !b.getAbreise().isAfter(von)) continue;
                if (b.getBuchungsnummer() != null) nummern.add(b.getBuchungsnummer());
                belegung.mengen().forEach((id, menge) -> {
                    alle.merge(id, menge, Integer::sum);
                    if (b.getStatus() != Buchungsstatus.RESERVIERT) feste.merge(id, menge, Integer::sum);
                });
            }
            for (MietobjektSperrzeit sperre : objekt.getSperrzeiten()) {
                if (SperrzeitService.ueberlappt(sperre.getVon(), sperre.getBis(), datum.atStartOfDay(), datum.plusDays(1).atStartOfDay())) {
                    zeitbelegungen = new ArrayList<>(zeitbelegungen);
                    zeitbelegungen.add(new com.kcserver.vermietung.dto.HomepageIntegrationDTO.KalenderBelegung(
                            sperre.getVon().isBefore(datum.atStartOfDay()) ? datum.atStartOfDay() : sperre.getVon(),
                            sperre.getBis().isAfter(datum.plusDays(1).atStartOfDay()) ? datum.plusDays(1).atStartOfDay() : sperre.getBis(),
                            "GESPERRT", null, null));
                }
            }
            boolean gesperrt = objekt.getSperrzeiten().stream().anyMatch(sperre ->
                    SperrzeitService.ueberlappt(sperre.getVon(), sperre.getBis(), von, bis));
            int frei = gesperrt || ueberschneidung ? 0 : ressourcen.values().stream()
                    .mapToInt(r -> Math.max(0, r.getBestand() - alle.getOrDefault(r.getId(), 0))).min().orElse(0);
            boolean festBelegt = ueberschneidung || ressourcen.values().stream()
                    .anyMatch(r -> feste.getOrDefault(r.getId(), 0) >= r.getBestand());
            tage.add(new com.kcserver.vermietung.dto.HomepageIntegrationDTO.KalenderTag(
                    datum, gesperrt ? "GESPERRT" : frei > 0 ? "FREI" : festBelegt ? "BELEGT" : "RESERVIERT", frei, List.copyOf(nummern), zeitbelegungen));
        }
        return new com.kcserver.vermietung.dto.HomepageIntegrationDTO.Kalender(monat.toString(), bereich.getId(), tage);
    }

    /** Der vollständige Mietername ist ausdrücklich für den öffentlichen Homepagekalender freigegeben. */
    private String homepageMietername(Buchung buchung) {
        if (buchung.getMieter() == null) return null;
        String vorname = Objects.toString(buchung.getMieter().getVorname(), "").trim();
        String nachname = Objects.toString(buchung.getMieter().getName(), "").trim();
        String vollstaendig = (vorname + " " + nachname).trim();
        return vollstaendig.isEmpty() ? null : vollstaendig;
    }

    private void kalenderVoraussetzungen(Mietbereich b, Set<Long> freigegeben, Set<Mietbereich> auswahl) {
        if (!freigegeben.contains(b.getId()) || !b.isMietbar() || !b.isDirektbuchungAktiv())
            throw new IllegalArgumentException("Ein erforderlicher Mietbereich ist online nicht freigegeben.");
        if (!auswahl.add(b)) return;
        for (Mietbereich voraussetzung : b.getVoraussetzungenDirekt()) kalenderVoraussetzungen(voraussetzung, freigegeben, auswahl);
    }

    private Set<Mietbereich> ermittleBelegteRessourcen(
            Mietbereich mietbereich,
            Set<Long> besucht
    ) {
        if (!besucht.add(mietbereich.getId())) {
            throw new IllegalStateException("Zyklische Belegungszusammensetzung bei '"
                    + mietbereich.getBezeichnung() + "'.");
        }
        Set<Mietbereich> komponenten = mietbereich.getBelegteMietbereiche();
        if (komponenten == null || komponenten.isEmpty()) {
            return Set.of(mietbereich);
        }

        Set<Mietbereich> ressourcen = new HashSet<>();
        ressourcen.add(mietbereich);
        for (Mietbereich komponent : komponenten) {
            ressourcen.addAll(ermittleBelegteRessourcen(komponent, new HashSet<>(besucht)));
        }
        return ressourcen;
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
