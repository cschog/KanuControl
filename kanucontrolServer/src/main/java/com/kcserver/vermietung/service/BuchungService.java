package com.kcserver.vermietung.service;

import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.kjfp.entity.Person;
import com.kcserver.kjfp.entity.Verein;
import com.kcserver.kjfp.repository.PersonRepository;
import com.kcserver.kjfp.repository.VereinRepository;
import com.kcserver.vermietung.dto.BuchungDTO;
import com.kcserver.vermietung.dto.BuchungMietbereichDTO;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsquelle;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.repository.BuchungRepository;
import com.kcserver.vermietung.repository.BuchungsnummerCounterRepository;
import com.kcserver.vermietung.repository.MietbereichRepository;
import com.kcserver.vermietung.repository.MietobjektRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;

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

    private static final java.time.LocalDate UNBEFRISTET_BIS =
            java.time.LocalDate.of(3000, 12, 31);

    public BuchungService(
            BuchungRepository buchungRepository,
            BuchungsnummerCounterRepository buchungsnummerCounterRepository,
            PersonRepository personRepository,
            VereinRepository vereinRepository,
            MietobjektRepository mietobjektRepository,
            MietbereichRepository mietbereichRepository
    ) {
        this.buchungRepository = buchungRepository;
        this.buchungsnummerCounterRepository =
                buchungsnummerCounterRepository;
        this.personRepository = personRepository;
        this.vereinRepository = vereinRepository;
        this.mietobjektRepository = mietobjektRepository;
        this.mietbereichRepository = mietbereichRepository;
    }

    @Transactional(readOnly = true)
    public List<BuchungDTO> getAll() {
        return buchungRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
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

        return toDTO(buchung);
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

        buchung.setAnreise(dto.getAnreise());
        buchung.setAbreise(
                dto.isUnbefristet()
                        ? UNBEFRISTET_BIS
                        : dto.getAbreise()
        );

        buchung.setStatus(
                com.kcserver.vermietung.enumtype.Buchungsstatus.ANFRAGE
        );

        setzeBuchungspositionen(
                buchung,
                dto,
                mietobjekt,
                buchung.getBuchungsquelle()
        );

        vergebeBuchungsnummer(buchung);

        return toDTO(
                buchungRepository.save(buchung)
        );
    }

    public BuchungDTO update(Long id, BuchungDTO dto) {

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

        Person mieter = getMieter(dto.getMieterId());

        Mietobjekt mietobjekt =
                getMietobjekt(dto.getMietobjektId());

        Verein veranstalter =
                getVeranstalter(dto.getVeranstalterVereinId());

        // Neue Stammdaten setzen.
        buchung.setMieter(mieter);
        buchung.setMietobjekt(mietobjekt);
        buchung.setVeranstalter(veranstalter);
        buchung.setAnreise(dto.getAnreise());
        buchung.setAbreise(
                dto.isUnbefristet()
                        ? UNBEFRISTET_BIS
                        : dto.getAbreise()
        );
        buchung.setBuchungsquelle(
                dto.getBuchungsquelle() != null
                        ? dto.getBuchungsquelle()
                        : buchung.getBuchungsquelle()
        );

// Bestehende Zuordnungen entfernen.
        buchung.getMietbereiche().clear();
        buchungRepository.flush();

// Neue Zuordnungen anlegen und Buchungsquelle prüfen.
        setzeBuchungspositionen(
                buchung,
                dto,
                mietobjekt,
                dto.getBuchungsquelle()
        );

// Status bestimmen und gegebenenfalls Bestand prüfen.
        Buchungsstatus neuerStatus = dto.getStatus() != null
                ? dto.getStatus()
                : buchung.getStatus();

        if (neuerStatus == Buchungsstatus.BESTAETIGT) {
            pruefeBestand(buchung, buchung.getId());
        }

        buchung.setStatus(neuerStatus);

        buchungRepository.flush();

        return toDTO(buchung);
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

        return dto;
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
        List<Buchung> bestehendeBuchungen;

        if (eigeneBuchungId == null) {
            bestehendeBuchungen =
                    buchungRepository
                            .findByStatusAndAnreiseLessThanAndAbreiseGreaterThan(
                                    Buchungsstatus.BESTAETIGT,
                                    buchung.getAbreise(),
                                    buchung.getAnreise()
                            );
        } else {
            bestehendeBuchungen =
                    buchungRepository
                            .findByStatusAndAnreiseLessThanAndAbreiseGreaterThanAndIdNot(
                                    Buchungsstatus.BESTAETIGT,
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
                                java.util.Locale.GERMAN,
                                com.kcserver.core.exception.ErrorMessages.BUCHUNG_BESTAND_UNZUREICHEND,
                                mietbereich.getBezeichnung(),
                                bestand,
                                bereitsGebucht,
                                angefordert
                        )
                );
            }
        }
    }
}