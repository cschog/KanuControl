package com.kcserver.service;

import com.kcserver.dto.person.PersonListDTO;
import com.kcserver.dto.veranstaltung.*;
import com.kcserver.entity.*;
import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.repository.VeranstaltungSpecs;

import com.kcserver.mapper.PersonMapper;
import com.kcserver.mapper.VeranstaltungMapper;
import com.kcserver.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import static com.kcserver.exception.ErrorMessages.*;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class VeranstaltungServiceImpl implements VeranstaltungService {

    private final VeranstaltungRepository veranstaltungRepository;
    private final VereinRepository vereinRepository;
    private final PersonRepository personRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final PlanungRepository planungRepository;
    private final VeranstaltungMapper veranstaltungMapper;
    private final PersonMapper personMapper;

    public VeranstaltungServiceImpl(
            VeranstaltungRepository veranstaltungRepository,
            VereinRepository vereinRepository,
            PersonRepository personRepository,
            TeilnehmerRepository teilnehmerRepository,
            PlanungRepository planungRepository,
            VeranstaltungMapper veranstaltungMapper,
            PersonMapper personMapper
    ) {
        this.veranstaltungRepository = veranstaltungRepository;
        this.vereinRepository = vereinRepository;
        this.personRepository = personRepository;
        this.teilnehmerRepository = teilnehmerRepository;
        this.planungRepository = planungRepository;
        this.veranstaltungMapper = veranstaltungMapper;
        this.personMapper = personMapper;
    }

    /* =========================================================
       CREATE
       ========================================================= */

    @Override
    public VeranstaltungDetailDTO create(VeranstaltungCreateDTO dto) {

        veranstaltungRepository.unsetAktiveVeranstaltung();
        veranstaltungRepository.flush();

        Verein verein = vereinRepository.findById(dto.getVereinId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Person leiter = personRepository.findById(dto.getLeiterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        validateLeiterAge(leiter);

        Veranstaltung v = new Veranstaltung();
        v.setName(dto.getName());
        v.setTyp(dto.getTyp());
        v.setBeginnDatum(dto.getBeginnDatum());
        v.setEndeDatum(dto.getEndeDatum());
        v.setBeginnZeit(dto.getBeginnZeit());
        v.setEndeZeit(dto.getEndeZeit());
        v.setVerein(verein);
        v.setLeiter(leiter);
        v.setAktiv(true);

        Veranstaltung saved = veranstaltungRepository.save(v);

        // Leiter als Teilnehmer sicherstellen
        Teilnehmer t = new Teilnehmer();
        t.setVeranstaltung(saved);
        t.setPerson(leiter);
        t.setRolle(TeilnehmerRolle.LEITER);
        teilnehmerRepository.save(t);

        return veranstaltungMapper.toDetailDTO(saved);
    }

    /* =========================================================
       READ
       ========================================================= */

    @Override
    @Transactional(readOnly = true)
    public VeranstaltungDetailDTO getById(Long id) {
        return veranstaltungMapper.toDetailDTO(
                veranstaltungRepository.findByIdWithRelations(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<VeranstaltungListDTO> getAll() {
        return veranstaltungRepository.findAll()
                .stream()
                .map(veranstaltungMapper::toListDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VeranstaltungListDTO> search(
            VeranstaltungFilterDTO filter,
            Pageable pageable
    ) {
        return veranstaltungRepository.findAll(
                VeranstaltungSpecs.filter(filter),
                pageable
        ).map(veranstaltungMapper::toListDTO);
    }

    /* =========================================================
       AVAILABLE / ASSIGNED
       ========================================================= */

    @Override
    @Transactional(readOnly = true)
    public List<PersonListDTO> getAvailablePersons(Long veranstaltungId, String search) {

        checkVeranstaltungExists(veranstaltungId);

        String searchTerm = (search == null || search.isBlank()) ? "" : search.trim();

        return teilnehmerRepository
                .findAvailable(veranstaltungId, safe(searchTerm))
                .stream()
                .map(personMapper::toListDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonListDTO> getAssignedPersons(Long veranstaltungId) {

        return teilnehmerRepository
                .findAllWithPerson(veranstaltungId)
                .stream()
                .map(t -> personMapper.toListDTO(t.getPerson()))
                .toList();
    }


    @Transactional(readOnly = true)
    public List<VeranstaltungListDTO> searchAll(
            VeranstaltungFilterDTO filter
    ) {
        return veranstaltungRepository.findAll(
                        VeranstaltungSpecs.filter(filter)
                )
                .stream()
                .map(veranstaltungMapper::toListDTO)
                .toList();
    }

    /* =========================================================
       BULK
       ========================================================= */

    @Override
    public void addTeilnehmerBulk(Long veranstaltungId, List<Long> personIds) {

        Veranstaltung v = getVeranstaltungOrThrow(veranstaltungId);

        List<Person> persons = personRepository.findAllById(personIds);

        for (Person p : persons) {

            boolean exists = teilnehmerRepository
                    .findByVeranstaltungAndPerson(v, p)
                    .isPresent();

            if (!exists) {
                Teilnehmer t = new Teilnehmer();
                t.setVeranstaltung(v);
                t.setPerson(p);
                teilnehmerRepository.save(t);
            }
        }
    }

    @Override
    public void removeTeilnehmerBulk(Long veranstaltungId, List<Long> personIds) {

        Veranstaltung v = getVeranstaltungOrThrow(veranstaltungId);

        for (Long personId : personIds) {

            if (personIds == null || personIds.isEmpty()) return;

            Person p = personRepository.findById(personId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            teilnehmerRepository
                    .findByVeranstaltungAndPerson(v, p)
                    .ifPresent(t -> {

                        if (t.getRolle() == TeilnehmerRolle.LEITER) {
                            throw new ResponseStatusException(HttpStatus.CONFLICT);
                        }

                        teilnehmerRepository.delete(t);
                    });
        }
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @Override
    public void delete(Long id) {

        Veranstaltung v = getVeranstaltungOrThrow(id);

        boolean hasTeilnehmer =
                teilnehmerRepository.existsNonLeiter(id, TeilnehmerRolle.LEITER);

        if (hasTeilnehmer) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        planungRepository.findByVeranstaltungId(id)
                .ifPresent(planungRepository::delete);

        veranstaltungRepository.delete(v);
    }



    /* =========================================================
       HELPER
       ========================================================= */

    private Veranstaltung getVeranstaltungOrThrow(Long id) {
        return veranstaltungRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void checkVeranstaltungExists(Long id) {
        if (!veranstaltungRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void validateLeiterAge(Person person) {
        if (person.getGeburtsdatum() == null ||
                person.getGeburtsdatum().plusYears(18).isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
    @Override
    @Transactional(readOnly = true)
    public VeranstaltungDetailDTO getActive() {
        return veranstaltungRepository.findByAktivTrue()
                .map(veranstaltungMapper::toDetailDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No active Veranstaltung"
                ));
    }
    @Override
    @Transactional(readOnly = true)
    public Optional<VeranstaltungDetailDTO> getActiveOptional() {
        return veranstaltungRepository.findByAktivTrue()
                .map(veranstaltungMapper::toDetailDTO);
    }

    @Override
    public VeranstaltungDetailDTO setActive(Long veranstaltungId) {

        Veranstaltung neu = veranstaltungRepository.findById(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // alle deaktivieren
        veranstaltungRepository.unsetAktiveVeranstaltung();
        veranstaltungRepository.flush();

        // aktiv setzen
        neu.setAktiv(true);
        veranstaltungRepository.save(neu);

        // 🔥 ENTSCHEIDEND: neu laden mit JOIN FETCH
        Veranstaltung loaded = veranstaltungRepository
                .findByIdWithRelations(neu.getId())
                .orElseThrow();

        return veranstaltungMapper.toDetailDTO(loaded);
    }

    @Override
    @Transactional
    public VeranstaltungDetailDTO update(Long id, VeranstaltungUpdateDTO dto) {

        Veranstaltung v = veranstaltungRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, VERANSTALTUNG_NOT_FOUND
                ));

    /* =========================
       SIMPLE FIELDS
       ========================= */

        if (dto.getName() != null) {
            v.setName(dto.getName());
        }

        if (dto.getBeginnDatum() != null) {
            v.setBeginnDatum(dto.getBeginnDatum());
        }

        if (dto.getEndeDatum() != null) {
            v.setEndeDatum(dto.getEndeDatum());
        }

        if (dto.getBeginnZeit() != null) {
            v.setBeginnZeit(dto.getBeginnZeit());
        }

        if (dto.getEndeZeit() != null) {
            v.setEndeZeit(dto.getEndeZeit());
        }

    /* =========================
       VEREIN
       ========================= */

        if (dto.getVereinId() != null) {

            Verein verein = vereinRepository.findById(dto.getVereinId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, VEREIN_NOT_FOUND
                    ));

            v.setVerein(verein);
        }

    /* =========================
       LEITER (inkl. Validierung)
       ========================= */

        if (dto.getLeiterId() != null) {

            Person leiter = personRepository.findById(dto.getLeiterId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, PERSON_NOT_FOUND
                    ));

            validateLeiterAge(leiter);   // ⭐ DAS HAT GEFEHLT

            v.setLeiter(leiter);
        }

        return veranstaltungMapper.toDetailDTO(v);
    }
}