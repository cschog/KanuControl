package com.kcserver.service;

import com.kcserver.dto.TeilnehmerDTO;
import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.mapper.TeilnehmerMapper;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class TeilnehmerService {

    private final TeilnehmerRepository teilnehmerRepository;
    private final VeranstaltungRepository veranstaltungRepository;
    private final PersonRepository personRepository;
    private final TeilnehmerMapper teilnehmerMapper;

    public TeilnehmerService(
            TeilnehmerRepository teilnehmerRepository,
            VeranstaltungRepository veranstaltungRepository,
            PersonRepository personRepository,
            TeilnehmerMapper teilnehmerMapper
    ) {
        this.teilnehmerRepository = teilnehmerRepository;
        this.veranstaltungRepository = veranstaltungRepository;
        this.personRepository = personRepository;
        this.teilnehmerMapper = teilnehmerMapper;
    }

    /* =========================================================
       READ
       ========================================================= */

    public List<TeilnehmerDTO> getTeilnehmerDerAktivenVeranstaltung() {
        Veranstaltung veranstaltung = getAktiveVeranstaltung();

        return teilnehmerRepository.findByVeranstaltung(veranstaltung)
                .stream()
                .map(teilnehmerMapper::toDTO)
                .toList();
    }

    /* =========================================================
       CREATE (UC-T1)
       ========================================================= */

    public TeilnehmerDTO addTeilnehmerZurAktivenVeranstaltung(TeilnehmerDTO dto) {

        Veranstaltung veranstaltung = getAktiveVeranstaltung();
        Person person = getPerson(dto.getPersonId());

        // 🔒 darf nur einmal Teilnehmer sein
        teilnehmerRepository
                .findByVeranstaltungAndPerson(veranstaltung, person)
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Person is already Teilnehmer of the active Veranstaltung"
                    );
                });

        Teilnehmer teilnehmer = new Teilnehmer();
        teilnehmer.setVeranstaltung(veranstaltung);
        teilnehmer.setPerson(person);
        teilnehmer.setRolle(
                dto.getRolle() != null
                        ? dto.getRolle()
                        : TeilnehmerRolle.TEILNEHMER
        );

        return teilnehmerMapper.toDTO(
                teilnehmerRepository.save(teilnehmer)
        );
    }

    /* =========================================================
       DELETE
       ========================================================= */

    public void removeTeilnehmerVonAktiverVeranstaltung(Long personId) {

        Veranstaltung veranstaltung = getAktiveVeranstaltung();
        Person person = getPerson(personId);

        Teilnehmer teilnehmer = teilnehmerRepository
                .findByVeranstaltungAndPerson(veranstaltung, person)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Teilnehmer not found"
                ));

        if (teilnehmer.getRolle() == TeilnehmerRolle.LEITER) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Leiter cannot be removed. Assign another Leiter first."
            );
        }

        teilnehmerRepository.delete(teilnehmer);
    }

    /* =========================================================
       LEITER
       ========================================================= */

    public TeilnehmerDTO setLeiterDerAktivenVeranstaltung(Long personId) {

        Veranstaltung veranstaltung = getAktiveVeranstaltung();
        Person person = getPerson(personId);

        validateLeiterAge(person);

        // alten Leiter zurücksetzen
        teilnehmerRepository
                .findByVeranstaltungAndRolle(
                        veranstaltung,
                        TeilnehmerRolle.LEITER
                )
                .ifPresent(existing -> {
                    existing.setRolle(TeilnehmerRolle.TEILNEHMER);
                    teilnehmerRepository.save(existing);
                });

        // Leiter muss Teilnehmer sein
        Teilnehmer teilnehmer = teilnehmerRepository
                .findByVeranstaltungAndPerson(veranstaltung, person)
                .orElseGet(() -> {
                    Teilnehmer t = new Teilnehmer();
                    t.setVeranstaltung(veranstaltung);
                    t.setPerson(person);
                    t.setRolle(TeilnehmerRolle.TEILNEHMER);
                    return t;
                });

        teilnehmer.setRolle(TeilnehmerRolle.LEITER);

        return teilnehmerMapper.toDTO(
                teilnehmerRepository.save(teilnehmer)
        );
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private Veranstaltung getAktiveVeranstaltung() {
        return veranstaltungRepository.findByAktivTrue()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "No active Veranstaltung found"
                ));
    }

    private Person getPerson(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Person not found"
                ));
    }

    private void validateLeiterAge(Person person) {
        if (person.getGeburtsdatum() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Geburtsdatum required for Leiter"
            );
        }

        if (person.getGeburtsdatum()
                .plusYears(18)
                .isAfter(LocalDate.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Leiter must be at least 18 years old"
            );
        }
    }
}