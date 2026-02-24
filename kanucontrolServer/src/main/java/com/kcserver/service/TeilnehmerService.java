package com.kcserver.service;

import com.kcserver.dto.person.PersonListDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerDetailDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerListDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerUpdateDTO;
import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.mapper.PersonMapper;
import com.kcserver.mapper.TeilnehmerMapper;
import com.kcserver.repository.MitgliedRepository;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final PersonMapper personMapper;
    private final MitgliedRepository mitgliedRepository;

    public TeilnehmerService(
            TeilnehmerRepository teilnehmerRepository,
            VeranstaltungRepository veranstaltungRepository,
            PersonRepository personRepository,
            MitgliedRepository mitgliedRepository,
            TeilnehmerMapper teilnehmerMapper,
            PersonMapper personMapper
    ) {
        this.teilnehmerRepository = teilnehmerRepository;
        this.veranstaltungRepository = veranstaltungRepository;
        this.personRepository = personRepository;
        this.mitgliedRepository = mitgliedRepository;
        this.teilnehmerMapper = teilnehmerMapper;
        this.personMapper = personMapper;
    }
    /* =========================================================
       READ (Paged)
       ========================================================= */

    @Transactional(readOnly = true)
    public Page<TeilnehmerListDTO> getTeilnehmer(Long veranstaltungId, Pageable pageable) {

        Veranstaltung veranstaltung = veranstaltungRepository.findByIdWithRelations(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Veranstaltung not found"
                ));

        return teilnehmerRepository
                .findWithPersonByVeranstaltung(veranstaltung, pageable)
                .map(teilnehmerMapper::toListDTO);
    }

    /* =========================================================
       READ (Full List)
       ========================================================= */

    @Transactional(readOnly = true)
    public List<TeilnehmerDetailDTO> getTeilnehmerForVeranstaltung(Long veranstaltungId) {

        veranstaltungRepository.findById(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Veranstaltung not found"
                ));

        return teilnehmerRepository
                .findByVeranstaltungWithPerson(veranstaltungId)
                .stream()
                .map(teilnehmerMapper::toDetailDTO)
                .toList();
    }

    /* =========================================================
       ADD SINGLE
       ========================================================= */

    public TeilnehmerDetailDTO addTeilnehmer(Long veranstaltungId, Long personId) {

        Veranstaltung veranstaltung = veranstaltungRepository.findByIdWithRelations(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Veranstaltung not found"
                ));

        Person person = getPerson(personId);

        // ❗ Duplicate verhindern → 409
        if (teilnehmerRepository
                .findByVeranstaltungAndPerson(veranstaltung, person)
                .isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Person is already Teilnehmer"
            );
        }

        Teilnehmer teilnehmer = new Teilnehmer();
        teilnehmer.setVeranstaltung(veranstaltung);
        teilnehmer.setPerson(person);

// ⭐ Neue Logik
        boolean hasFunktion = mitgliedRepository
                .existsByPerson_IdAndFunktionIsNotNull(person.getId());

        if (hasFunktion) {
            teilnehmer.setRolle(TeilnehmerRolle.MITARBEITER);
        } else {
            teilnehmer.setRolle(null);
        }

        return teilnehmerMapper.toDetailDTO(
                teilnehmerRepository.save(teilnehmer)
        );
    }

    /* =========================================================
       ADD BULK
       ========================================================= */

    public void addTeilnehmerBulk(Long veranstaltungId, List<Long> personIds) {

        Veranstaltung veranstaltung = veranstaltungRepository.findByIdWithRelations(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Veranstaltung not found"
                ));

        for (Long personId : personIds) {

            Person person = getPerson(personId);

            boolean exists = teilnehmerRepository
                    .findByVeranstaltungAndPerson(veranstaltung, person)
                    .isPresent();

            if (!exists) {
                Teilnehmer t = new Teilnehmer();
                t.setVeranstaltung(veranstaltung);
                t.setPerson(person);
                boolean hasFunktion = mitgliedRepository
                        .existsByPerson_IdAndFunktionIsNotNull(person.getId());

                t.setRolle(hasFunktion ? TeilnehmerRolle.MITARBEITER : null);
                teilnehmerRepository.save(t);
            }
        }
    }
    /* =========================================================
     UPDATE
     ========================================================= */
    public TeilnehmerDetailDTO update(
            Long veranstaltungId,
            Long teilnehmerId,
            TeilnehmerUpdateDTO dto
    ) {

        Teilnehmer t = teilnehmerRepository.findById(teilnehmerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!t.getVeranstaltung().getId().equals(veranstaltungId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Teilnehmer does not belong to Veranstaltung"
            );
        }

        if (dto.getRolle() != null) {

            // Leiter darf nicht überschrieben werden
            if (t.getRolle() == TeilnehmerRolle.LEITER) {
                return teilnehmerMapper.toDetailDTO(t);
            }

            if (dto.getRolle() == TeilnehmerRolle.LEITER) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Leiter must be set via Veranstaltung"
                );
            }

            t.setRolle(dto.getRolle());
        }

        return teilnehmerMapper.toDetailDTO(t);
    }

    /* =========================================================
       UPDATE ROLE
       ========================================================= */

    public void updateRolle(Long veranstaltungId, Long personId, TeilnehmerRolle rolle) {

        Teilnehmer t = teilnehmerRepository
                .findByVeranstaltungIdAndPersonId(veranstaltungId, personId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Teilnehmer not found"
                ));

        // ❗ Leiter darf NICHT überschrieben werden
        if (t.getRolle() == TeilnehmerRolle.LEITER) {
            return;
        }

        // nur null oder MITARBEITER erlaubt
        if (rolle == TeilnehmerRolle.LEITER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Leiter must be set via Veranstaltung"
            );
        }

        t.setRolle(rolle);   // null oder MITARBEITER
    }

    /* =========================================================
       DELETE SINGLE
       ========================================================= */

    public void removeTeilnehmer(Long veranstaltungId, Long teilnehmerId) {

        Teilnehmer teilnehmer = teilnehmerRepository.findById(teilnehmerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Teilnehmer not found"
                ));

        if (!teilnehmer.getVeranstaltung().getId().equals(veranstaltungId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Teilnehmer does not belong to Veranstaltung"
            );
        }

        if (teilnehmer.getRolle() == TeilnehmerRolle.LEITER) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Leiter cannot be removed. Assign another Leiter first."
            );
        }

        teilnehmerRepository.delete(teilnehmer);
    }

    /* =========================================================
       DELETE BULK
       ========================================================= */

    public void removeTeilnehmerBulk(Long veranstaltungId, List<Long> personIds) {

        Veranstaltung v = veranstaltungRepository.findByIdWithRelations(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Veranstaltung not found"
                ));

        Long leiterId = v.getLeiter().getId();

        List<Long> filteredIds = personIds.stream()
                .filter(id -> !id.equals(leiterId))
                .toList();

        if (filteredIds.isEmpty()) return;

        teilnehmerRepository.deleteByVeranstaltungIdAndPersonIds(
                veranstaltungId,
                filteredIds
        );
    }

    /* =========================================================
       AVAILABLE PERSONS
       ========================================================= */

    @Transactional(readOnly = true)
    public Page<PersonListDTO> getAvailablePersons(
            Long veranstaltungId,
            String name,
            String vorname,
            String verein,
            Pageable pageable
    ) {

        veranstaltungRepository.findById(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Veranstaltung not found"
                ));

        return teilnehmerRepository
                .findAvailablePersonsFiltered(veranstaltungId, name, vorname, verein, pageable)
                .map(personMapper::toListDTO);
    }

    /* =========================================================
   ASSIGNED PERSONS (für FE Dual-List)
   ========================================================= */

    @Transactional(readOnly = true)
    public List<PersonListDTO> getAssignedPersons(Long veranstaltungId) {

        veranstaltungRepository.findById(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Veranstaltung not found"
                ));

        return teilnehmerRepository
                .findByVeranstaltungWithPerson(veranstaltungId)
                .stream()
                .map(t -> personMapper.toListDTO(t.getPerson()))
                .toList();
    }

    /* =========================================================
       SET LEITER
       ========================================================= */

    public TeilnehmerDetailDTO setLeiter(Long veranstaltungId, Long personId) {

        Veranstaltung veranstaltung = veranstaltungRepository.findByIdWithRelations(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Veranstaltung not found"
                ));

        Person person = getPerson(personId);

        validateLeiterAge(person);

        // alten Leiter zurücksetzen
        teilnehmerRepository
                .findByVeranstaltungAndRolle(veranstaltung, TeilnehmerRolle.LEITER)
                .ifPresent(existing -> {
                    existing.setRolle(null);
                    teilnehmerRepository.save(existing);
                });

        Teilnehmer teilnehmer = teilnehmerRepository
                .findByVeranstaltungAndPerson(veranstaltung, person)
                .orElseGet(() -> {
                    Teilnehmer t = new Teilnehmer();
                    t.setVeranstaltung(veranstaltung);
                    t.setPerson(person);
                    t.setRolle(null);
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

    private Person getPerson(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Person not found"
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