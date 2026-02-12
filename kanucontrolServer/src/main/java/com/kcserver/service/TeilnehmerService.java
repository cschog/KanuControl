package com.kcserver.service;

import com.kcserver.dto.teilnehmer.TeilnehmerDetailDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerListDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerUpdateDTO;
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

    public Page<TeilnehmerListDTO> getTeilnehmer(Long veranstaltungId, Pageable pageable) {
        Veranstaltung veranstaltung = veranstaltungRepository.findByIdWithRelations(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veranstaltung not found"));

        return teilnehmerRepository
                .findWithPersonByVeranstaltung(veranstaltung, pageable)
                .map(teilnehmerMapper::toListDTO);
    }

    /* =========================================================
       CREATE (UC-T1)
       ========================================================= */

    public TeilnehmerDetailDTO addTeilnehmer(Long veranstaltungId, Long personId) {
        Veranstaltung veranstaltung = veranstaltungRepository.findByIdWithRelations(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veranstaltung not found"));

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
            Teilnehmer existing = teilnehmerRepository
                    .findByVeranstaltungAndPerson(veranstaltung, person)
                    .orElseThrow();
            return teilnehmerMapper.toDetailDTO(existing);
        }

        Teilnehmer teilnehmer = new Teilnehmer();
        teilnehmer.setVeranstaltung(veranstaltung);
        teilnehmer.setPerson(person);
        teilnehmer.setRolle(null);

        Teilnehmer saved = teilnehmerRepository.save(teilnehmer);
        teilnehmerRepository.flush();
        return teilnehmerMapper.toDetailDTO(saved);
    }
       /* =========================================================
       UPDATE
       ========================================================= */

    public TeilnehmerDetailDTO update(Long veranstaltungId, Long id, TeilnehmerUpdateDTO dto) {

        Teilnehmer t = teilnehmerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!t.getVeranstaltung().getId().equals(veranstaltungId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Teilnehmer does not belong to Veranstaltung");
        }

        if (dto.getRolle() != null) {
            t.setRolle(dto.getRolle());
        }

        return teilnehmerMapper.toDetailDTO(t);
    }

    /* =========================================================
       DELETE
       ========================================================= */

    public void removeTeilnehmer(Long veranstaltungId, Long teilnehmerId) {

        Teilnehmer teilnehmer = teilnehmerRepository.findById(teilnehmerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Teilnehmer not found"
                ));

        if (!teilnehmer.getVeranstaltung().getId().equals(veranstaltungId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Teilnehmer does not belong to Veranstaltung");
        }

        if (teilnehmer.getRolle() == TeilnehmerRolle.LEITER) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Leiter cannot be removed. Assign another Leiter first."
            );
        }

        teilnehmerRepository.delete(teilnehmer);
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

    public TeilnehmerDetailDTO setLeiter(Long veranstaltungId, Long personId) {

        Veranstaltung veranstaltung = veranstaltungRepository.findByIdWithRelations(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Veranstaltung not found"
                ));

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
                    teilnehmerRepository.flush();
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

        Teilnehmer saved = teilnehmerRepository.save(teilnehmer);
        teilnehmerRepository.flush();
        return teilnehmerMapper.toDetailDTO(saved);
    }

    /* =========================================================
       HELPER
       ========================================================= */

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