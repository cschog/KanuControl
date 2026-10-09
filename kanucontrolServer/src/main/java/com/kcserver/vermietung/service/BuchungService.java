package com.kcserver.vermietung.service;

import com.kcserver.kjfp.entity.Person;
import com.kcserver.kjfp.entity.Verein;
import com.kcserver.kjfp.repository.PersonRepository;
import com.kcserver.kjfp.repository.VereinRepository;
import com.kcserver.vermietung.dto.BuchungDTO;
import com.kcserver.vermietung.entity.*;
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
                                "Buchung nicht gefunden: " + id
                        )
                );

        return toDTO(buchung);
    }

    public BuchungDTO create(BuchungDTO dto) {

        Person mieter = getMieter(dto.getMieterId());

        Mietobjekt mietobjekt =
                getMietobjekt(dto.getMietobjektId());

        List<Mietbereich> mietbereiche =
                getMietbereiche(dto.getMietbereichIds(), mietobjekt);

        Verein veranstalter =
                getVeranstalter(dto.getVeranstalterVereinId());

        Buchung buchung = new Buchung();

        buchung.setMieter(mieter);
        buchung.setMietobjekt(mietobjekt);
        buchung.setVeranstalter(veranstalter);

        buchung.setAnreise(dto.getAnreise());
        buchung.setAbreise(dto.getAbreise());

        buchung.setStatus(
                com.kcserver.vermietung.enumtype.Buchungsstatus.ANFRAGE
        );

        for (Mietbereich mietbereich : mietbereiche) {
            BuchungMietbereich zuordnung = new BuchungMietbereich();

            zuordnung.setBuchung(buchung);
            zuordnung.setMietbereich(mietbereich);

            buchung.getMietbereiche().add(zuordnung);
        }

        vergebeBuchungsnummer(buchung);

        return toDTO(
                buchungRepository.save(buchung)
        );
    }

    public BuchungDTO update(
            Long id,
            BuchungDTO dto
    ) {

        Buchung buchung = buchungRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Buchung nicht gefunden: " + id
                        )
                );

        Person mieter = getMieter(dto.getMieterId());

        Mietobjekt mietobjekt =
                getMietobjekt(dto.getMietobjektId());

        List<Mietbereich> mietbereiche =
                getMietbereiche(dto.getMietbereichIds(), mietobjekt);

        Verein veranstalter =
                getVeranstalter(dto.getVeranstalterVereinId());

        buchung.setMieter(mieter);
        buchung.setMietobjekt(mietobjekt);
        buchung.setVeranstalter(veranstalter);

        buchung.setAnreise(dto.getAnreise());
        buchung.setAbreise(dto.getAbreise());

        buchung.getMietbereiche().clear();

        for (Mietbereich mietbereich : mietbereiche) {
            BuchungMietbereich zuordnung = new BuchungMietbereich();

            zuordnung.setBuchung(buchung);
            zuordnung.setMietbereich(mietbereich);

            buchung.getMietbereiche().add(zuordnung);
        }

        if (dto.getStatus() != null) {
            buchung.setStatus(dto.getStatus());
        }

        // Buchungsnummer bleibt beim Update unverändert.

        return toDTO(
                buchungRepository.save(buchung)
        );
    }

    public void delete(Long id) {

        Buchung buchung = buchungRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Buchung nicht gefunden: " + id
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

        dto.setAnreise(buchung.getAnreise());
        dto.setAbreise(buchung.getAbreise());

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
}