package com.kcserver.service;

import com.kcserver.dto.mitglied.MitgliedSaveDTO;
import com.kcserver.dto.person.*;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.enumtype.CountryCode;
import com.kcserver.mapper.PersonMapper;
import com.kcserver.persistence.specification.PersonSpecification;
import com.kcserver.repository.MitgliedRepository;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VereinRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import com.kcserver.dto.common.ScrollResponse;

@Service
@Transactional
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final VereinRepository vereinRepository;
    private final MitgliedRepository mitgliedRepository;
    private final TeilnehmerRepository teilnehmerRepository;

    public PersonServiceImpl(
            PersonRepository personRepository,
            PersonMapper personMapper,
            VereinRepository vereinRepository,
            MitgliedRepository mitgliedRepository,
            TeilnehmerRepository teilnehmerRepository
    ) {
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.vereinRepository = vereinRepository;
        this.mitgliedRepository = mitgliedRepository;
        this.teilnehmerRepository = teilnehmerRepository;
    }

    /* =========================================================
       READ
       ========================================================= */
    @Override
    @Transactional(readOnly = true)
    public ScrollResponse<PersonListDTO> scroll(
            String cursorName,
            String cursorVorname,
            Long cursorId,
            int size,
            PersonSearchCriteria criteria
    ) {

        List<Person> result = personRepository.scroll(
                cursorName,
                cursorVorname,
                cursorId,
                criteria.getSearch(),
                criteria.getOrt()
        );

        long total = personRepository.count(
                PersonSpecification.byCriteria(criteria)
        );

        return new ScrollResponse<>(
                result.stream()
                        .limit(size)
                        .map(personMapper::toListDTO)
                        .toList(),
                total
        );
    }

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

    @Transactional(readOnly = true)
    public List<PersonRefDTO> searchRefList(String search) {
        return personRepository.searchRefList(search)
                .stream()
                .map(personMapper::toPersonRefDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonListDTO> getAll(Sort sort, PersonSearchCriteria criteria) {

        return personRepository
                .findAll(PersonSpecification.byCriteria(criteria), sort)
                .stream()
                .map(p -> {
                    PersonListDTO dto = personMapper.toListDTO(p);
                    dto.setMitgliedschaftenCount(
                            p.getMitgliedschaften() == null
                                    ? 0
                                    : p.getMitgliedschaften().size()
                    );
                    return dto;
                })
                .toList();
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

        // =====================================================
        // Mitgliedschaften speichern
        // =====================================================

        if (dto.getMitgliedschaften() != null) {

            for (MitgliedSaveDTO m : dto.getMitgliedschaften()) {

                Verein verein = vereinRepository.findById(m.getVereinId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Verein not found: " + m.getVereinId()
                        ));

                Mitglied mitglied = new Mitglied();

                mitglied.setPerson(saved);

                mitglied.setVerein(verein);

                mitglied.setFunktion(
                        m.getFunktion()
                );

                mitglied.setHauptVerein(
                        Boolean.TRUE.equals(m.getHauptVerein())
                );

                mitgliedRepository.save(mitglied);
            }
        }

        Person reloaded =
                personRepository.findDetailById(saved.getId())
                        .orElseThrow();

        return personMapper.toDetailDTO(reloaded);
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    @Override
    public PersonDetailDTO updatePerson(long id, PersonSaveDTO dto) {

        // 1. LOAD
        Person existing = personRepository.findDetailById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Person not found"
                ));

        // 2. MERGE (finale Werte!)
        String name = merge(dto.getName(), existing.getName());
        String vorname = merge(dto.getVorname(), existing.getVorname());
        LocalDate geburtsdatum = merge(dto.getGeburtsdatum(), existing.getGeburtsdatum());

        // 3. VALIDATE
        ensureUniquePerson(vorname, name, geburtsdatum, existing.getId());

        // 4. APPLY
        personMapper.updateFromDTO(dto, existing);

        // 5. EXTRA LOGIK
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
        teilnehmerRepository.deleteByPersonId(id);
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
                .map(p -> {
                    PersonListDTO dto = personMapper.toListDTO(p);
                    dto.setMitgliedschaftenCount(
                            p.getMitgliedschaften() == null ? 0 : p.getMitgliedschaften().size()
                    );
                    return dto;
                });
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
            Mitglied haupt = hauptvereine.getFirst();

            existing.forEach(m ->
                    m.setHauptVerein(m == haupt)
            );
        }
    }
    private <T> T merge(T dtoValue, T entityValue) {
        return dtoValue != null ? dtoValue : entityValue;
    }

    private void ensureUniquePerson(
            String vorname,
            String name,
            LocalDate geburtsdatum,
            Long currentId
    ) {
        personRepository
                .findByVornameAndNameAndGeburtsdatum(vorname, name, geburtsdatum)
                .filter(p -> !p.getId().equals(currentId))
                .ifPresent(p -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Eine andere Person mit gleichem Namen und Geburtsdatum existiert bereits"
                    );
                });
    }
}