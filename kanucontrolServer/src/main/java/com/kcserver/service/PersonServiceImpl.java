package com.kcserver.service;

import com.kcserver.dto.*;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.enumtype.CountryCode;
import com.kcserver.mapper.PersonMapper;
import com.kcserver.persistence.specification.PersonSpecification;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VereinRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.kcserver.dto.PersonListDTO;
import com.kcserver.dto.PersonDetailDTO;

import java.util.List;

@Service
@Transactional
public class PersonServiceImpl implements PersonService {

    private static final Logger logger =
            LoggerFactory.getLogger(PersonServiceImpl.class);

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final VereinRepository vereinRepository;

    public PersonServiceImpl(
            PersonRepository personRepository,
            PersonMapper personMapper,
            VereinRepository vereinRepository
    ) {
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.vereinRepository = vereinRepository;
    }

    /* =========================================================
       READ
       ========================================================= */

    @Override
    @Transactional(readOnly = true)
    public PersonDetailDTO getPersonDetail(long id) {

        Person person = personRepository.findDetailById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Person not found"
                ));

        return personMapper.toDetailDTO(person);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonListDTO> getAll(Pageable pageable) {
        return personRepository
                .findAll(pageable)
                .map(p -> {
                    PersonListDTO dto = personMapper.toListDTO(p);
                    dto.setMitgliedschaftenCount(
                            p.getMitgliedschaften() == null
                                    ? 0
                                    : p.getMitgliedschaften().size()
                    );
                    return dto;
                });
    }

    /* =========================================================
       CREATE
       ========================================================= */

    @Override
    public PersonDetailDTO createPerson(PersonSaveDTO dto) {

        if (dto.getGeburtsdatum() != null &&
                personRepository.existsByVornameAndNameAndGeburtsdatum(
                        dto.getVorname(),
                        dto.getName(),
                        dto.getGeburtsdatum()
                )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Eine Person mit gleichem Namen und Geburtsdatum existiert bereits"
            );
        }

        Person entity = personMapper.toNewEntity(dto);

        if (entity.getCountryCode() == null) {
            entity.setCountryCode(CountryCode.DE);
        }

        Person saved = personRepository.save(entity);

        return personMapper.toDetailDTO(saved);
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    @Override
    public PersonDetailDTO updatePerson(long id, PersonSaveDTO dto) {

        Person existing = personRepository.findDetailById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Person not found"
                ));

        if (dto.getGeburtsdatum() != null) {
            personRepository
                    .findByVornameAndNameAndGeburtsdatum(
                            dto.getVorname(),
                            dto.getName(),
                            dto.getGeburtsdatum()
                    )
                    .filter(p -> !p.getId().equals(existing.getId()))
                    .ifPresent(p -> {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Eine andere Person mit gleichem Namen und Geburtsdatum existiert bereits"
                        );
                    });
        }

        personMapper.updateFromDTO(dto, existing);

        syncMitgliedschaften(existing, dto.getMitgliedschaften());

        if (existing.getCountryCode() == null) {
            existing.setCountryCode(CountryCode.DE);
        }

        return personMapper.toDetailDTO(existing);
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @Override
    public void deletePerson(long id) {
        if (!personRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Person not found"
            );
        }
        personRepository.deleteById(id);
    }

    /* =========================================================
       SEARCH
       ========================================================= */

    @Override
    @Transactional(readOnly = true)
    public Page<PersonListDTO> searchList(
            PersonSearchCriteria criteria,
            Pageable pageable
    ) {
        return personRepository
                .findAll(PersonSpecification.byCriteria(criteria), pageable)
                .map(personMapper::toListDTO);
    }

    /* =========================================================
       Mitgliedschaften synchronisieren
       ========================================================= */

    private void syncMitgliedschaften(
            Person person,
            List<MitgliedSaveDTO> dtos
    ) {
        List<Mitglied> existing = person.getMitgliedschaften();

        if (dtos == null) {
            existing.clear();
            return;
        }

        // 1️⃣ Entfernte Mitgliedschaften löschen
        existing.removeIf(m ->
                dtos.stream().noneMatch(dto ->
                        dto.getVereinId().equals(m.getVerein().getId())
                )
        );

        // 2️⃣ Upsert (update oder neu)
        for (MitgliedSaveDTO dto : dtos) {

            Mitglied mitglied = existing.stream()
                    .filter(m -> m.getVerein().getId().equals(dto.getVereinId()))
                    .findFirst()
                    .orElseGet(() -> {
                        Verein verein = vereinRepository.findById(dto.getVereinId())
                                .orElseThrow(() -> new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Verein not found: " + dto.getVereinId()
                                ));

                        Mitglied m = new Mitglied();
                        m.setPerson(person);
                        m.setVerein(verein);
                        existing.add(m);
                        return m;
                    });

            mitglied.setFunktion(dto.getFunktion());
            mitglied.setHauptVerein(Boolean.TRUE.equals(dto.getHauptVerein()));
        }

        // 3️⃣ 🔑 HAUPTVEREIN-REGEL
        List<Mitglied> hauptvereine = existing.stream()
                .filter(Mitglied::getHauptVerein)
                .toList();

        if (hauptvereine.size() > 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Eine Person darf nur einen Hauptverein haben"
            );
        }

        if (hauptvereine.size() == 1) {
            Mitglied haupt = hauptvereine.get(0);

            existing.forEach(m ->
                    m.setHauptVerein(m == haupt)
            );
        }
    }
}