package com.kcserver.service;

import com.kcserver.dto.teilnehmer.TeilnehmerDetailDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerListDTO;
import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.mapper.TeilnehmerMapper;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<TeilnehmerListDTO> getTeilnehmerDerAktivenVeranstaltung(
            Pageable pageable
    ) {
        Veranstaltung veranstaltung = getAktiveVeranstaltung();

        return teilnehmerRepository
                .findWithPersonByVeranstaltung(veranstaltung, pageable)
                .map(teilnehmerMapper::toListDTO);
    }

    /* =========================================================
       CREATE (UC-T1)
       ========================================================= */

    public TeilnehmerDetailDTO addTeilnehmerZurAktivenVeranstaltung(
            Long personId
    ) {
        Veranstaltung veranstaltung = getAktiveVeranstaltung();
        Person person = getPerson(personId);

        teilnehmerRepository
                .findByVeranstaltungAndPerson(veranstaltung, person)
                .ifPresent(t -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Person is already Teilnehmer"
                    );
                });

        if (veranstaltung.getLeiter().getId().equals(personId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Leiter ist bereits Teilnehmer der Veranstaltung"
            );
        }

        Teilnehmer teilnehmer = new Teilnehmer();
        teilnehmer.setVeranstaltung(veranstaltung);
        teilnehmer.setPerson(person);
        teilnehmer.setRolle(null); // normaler Teilnehmer

        return teilnehmerMapper.toDetailDTO(
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


    @Transactional
    public void removeTeilnehmerBulkVonAktiverVeranstaltung(List<Long> personIds) {

        Veranstaltung aktiveVeranstaltung = veranstaltungRepository
                .findByAktivTrue()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Keine aktive Veranstaltung"
                ));

        Long leiterId = aktiveVeranstaltung.getLeiter().getId();

        List<Long> filtered = personIds.stream()
                .filter(id -> !id.equals(leiterId))
                .toList();

        if (filtered.isEmpty()) {
            return;
        }

        teilnehmerRepository.deleteByVeranstaltungIdAndPersonIds(
                aktiveVeranstaltung.getId(),
                filtered
        );
    }

    @Transactional
    public void removeTeilnehmerBulk(Long veranstaltungId, List<Long> personIds) {

        Veranstaltung v = veranstaltungRepository.findByIdWithRelations(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Veranstaltung not found"
                ));

        Long leiterId = v.getLeiter().getId();

        // 🔥 Leiter IMMER ausschließen
        List<Long> filteredIds = personIds.stream()
                .filter(id -> !id.equals(leiterId))
                .toList();

        if (filteredIds.isEmpty()) {
            return; // nichts zu tun, aber auch kein Fehler
        }

        teilnehmerRepository.deleteByVeranstaltungIdAndPersonIds(
                veranstaltungId,
                filteredIds
        );
    }

    /* =========================================================
       LEITER
       ========================================================= */

    public TeilnehmerDetailDTO setLeiterDerAktivenVeranstaltung(Long personId) {

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
                    existing.setRolle(null);
                    teilnehmerRepository.save(existing);
                });

        // Leiter muss Teilnehmer sein
        Teilnehmer teilnehmer = teilnehmerRepository
                .findByVeranstaltungAndPerson(veranstaltung, person)
                .orElseGet(() -> {
                    Teilnehmer t = new Teilnehmer();
                    t.setVeranstaltung(veranstaltung);
                    t.setPerson(person);
                    t.setRolle(null); // erstmal normaler Teilnehmer
                    return t;
                });

        teilnehmer.setRolle(TeilnehmerRolle.LEITER);

        return teilnehmerMapper.toDetailDTO(
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