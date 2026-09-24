package com.kcserver.service.person;

import com.kcserver.dto.mitglied.MitgliedSaveDTO;
import com.kcserver.dto.person.*;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Verein;
import com.kcserver.enumtype.CountryCode;
import com.kcserver.exception.ErrorMessages;
import com.kcserver.mapper.PersonMapper;
import com.kcserver.persistence.specification.PersonSpecification;
import com.kcserver.repository.*;
import com.kcserver.repository.fahrkosten.FahrtabschnittMitfahrerRepository;
import com.kcserver.repository.fahrkosten.ReisekostenabrechnungRepository;
import com.kcserver.service.BankLookupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.kcserver.dto.common.ScrollResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

@Service
@Transactional
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final VereinRepository vereinRepository;
    private final MitgliedRepository mitgliedRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final ReisekostenabrechnungRepository reisekostenabrechnungRepository;
    private final FahrtabschnittMitfahrerRepository fahrtabschnittMitfahrerRepository;
    private final BankLookupService bankLookupService;
    private final PersonDataStatusService personDataStatusService;
    private final VeranstaltungRepository veranstaltungRepository;

    public PersonServiceImpl(
            PersonRepository personRepository,
            PersonMapper personMapper,
            VereinRepository vereinRepository,
            MitgliedRepository mitgliedRepository,
            TeilnehmerRepository teilnehmerRepository,
            ReisekostenabrechnungRepository reisekostenabrechnungRepository,
            FahrtabschnittMitfahrerRepository fahrtabschnittMitfahrerRepository,
            BankLookupService bankLookupService,
            PersonDataStatusService personDataStatusService,
            VeranstaltungRepository veranstaltungRepository
    ) {
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.vereinRepository = vereinRepository;
        this.mitgliedRepository = mitgliedRepository;
        this.teilnehmerRepository = teilnehmerRepository;
        this.reisekostenabrechnungRepository = reisekostenabrechnungRepository;
        this.fahrtabschnittMitfahrerRepository = fahrtabschnittMitfahrerRepository;
        this.bankLookupService = bankLookupService;
        this.personDataStatusService = personDataStatusService;
        this.veranstaltungRepository = veranstaltungRepository;
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

        boolean desc =
                "desc".equalsIgnoreCase(
                        criteria.getSortDirection()
                );

        Slice<Person> slice;

        if (desc) {

            slice = personRepository.scrollDesc(
                    cursorName,
                    cursorVorname,
                    cursorId,
                    criteria.getSearch(),
                    criteria.getOrt(),
                    criteria.getVereinId(),
                    criteria.getAktiv(),
                    PageRequest.of(0, size)
            );

        } else {

            slice = personRepository.scroll(
                    cursorName,
                    cursorVorname,
                    cursorId,
                    criteria.getSearch(),
                    criteria.getOrt(),
                    criteria.getVereinId(),
                    criteria.getAktiv(),
                    PageRequest.of(0, size)
            );
        }

        long total = personRepository.count(
                PersonSpecification.byCriteria(criteria)
        );

        List<Person> persons = slice.getContent();

        List<PersonListDTO> dtos = persons.stream()
                .map(personMapper::toListDTO)
                .toList();

        enrichListStatus(persons, dtos);

        return new ScrollResponse<>(
                dtos,
                total,
                slice.hasNext()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PersonDetailDTO getPersonDetail(long id) {

        Person person = personRepository.findDetailById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, ErrorMessages.PERSON_NOT_FOUND
                ));

        PersonDetailDTO dto = personMapper.toDetailDTO(person);
        dto.setDataStatus(determinePersonStatus(person));

        return dto;
    }

    private PersonDataStatusDTO determinePersonStatus(Person person) {

        boolean isLeiter =
                veranstaltungRepository
                        .findLeiterPersonIds(List.of(person.getId()))
                        .contains(person.getId());

        boolean isFahrer =
                reisekostenabrechnungRepository
                        .findFahrerPersonIds(List.of(person.getId()))
                        .contains(person.getId());

        return personDataStatusService.determineStatus(
                person,
                isLeiter,
                isFahrer
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonListDTO> getAll(Pageable pageable) {
        Page<Person> page = personRepository.findAll(pageable);

        List<Person> persons = page.getContent();

        List<PersonListDTO> dtos = persons.stream()
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

        enrichListStatus(persons, dtos);

        return new org.springframework.data.domain.PageImpl<>(
                dtos,
                pageable,
                page.getTotalElements()
        );
    }

    @Override
    public List<PersonRefDTO> searchRefList(
            String search,
            boolean nurLeiter,
            LocalDate stichtag
    ) {
        List<Person> persons;

        if (nurLeiter) {

            LocalDate referenzDatum =
                    stichtag != null
                            ? stichtag
                            : LocalDate.now();

            persons = personRepository.searchLeiterRefList(
                    search,
                    referenzDatum.minusYears(18)
            );

        } else {
            persons = personRepository.searchRefList(search);
        }

        return persons.stream()
                .map(personMapper::toPersonRefDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonListDTO> getAll(Sort sort, PersonSearchCriteria criteria) {

        List<Person> persons = personRepository
                .findAll(PersonSpecification.byCriteria(criteria), sort);

        List<PersonListDTO> dtos = persons.stream()
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

        enrichListStatus(persons, dtos);

        return dtos;
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
                    ErrorMessages.PERSON_ALREADY_EXISTS
            );
        }

        Person entity = personMapper.toNewEntity(dto);

        if (entity.getCountryCode() == null) {
            entity.setCountryCode(CountryCode.DE);
        }
        fillBankIfMissing(entity);

        Person saved = personRepository.save(entity);

        // =====================================================
        // Mitgliedschaften speichern
        // =====================================================

        if (dto.getMitgliedschaften() != null) {

            for (MitgliedSaveDTO m : dto.getMitgliedschaften()) {

                Verein verein = vereinRepository.findById(m.getVereinId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                ErrorMessages.VEREIN_NOT_FOUND
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

        PersonDetailDTO result = personMapper.toDetailDTO(reloaded);
        result.setDataStatus(determinePersonStatus(reloaded));

        return result;
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    @Override
    public PersonDetailDTO updatePerson(long id, PersonSaveDTO dto) {

        // 1. LOAD
        Person existing = personRepository.findDetailById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, ErrorMessages.PERSON_NOT_FOUND
                ));

        // 2. MERGE (finale Werte!)
        String name = merge(dto.getName(), existing.getName());
        String vorname = merge(dto.getVorname(), existing.getVorname());
        LocalDate geburtsdatum = merge(dto.getGeburtsdatum(), existing.getGeburtsdatum());

        // 3. VALIDATE
        ensureUniquePerson(vorname, name, geburtsdatum, existing.getId());

        // 4. APPLY
        personMapper.updateFromDTO(dto, existing);

        fillBankIfMissing(existing);

        // 5. EXTRA LOGIK
        syncMitgliedschaften(existing, dto.getMitgliedschaften());

        if (existing.getCountryCode() == null) {
            existing.setCountryCode(CountryCode.DE);
        }

        PersonDetailDTO result = personMapper.toDetailDTO(existing);
        result.setDataStatus(determinePersonStatus(existing));

        return result;
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @Override
    public void deletePerson(long id) {
        if (!personRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ErrorMessages.PERSON_NOT_FOUND
            );
        }

        List<Teilnehmer> teilnahmen =
                teilnehmerRepository.findByPersonId(id);

        if (!teilnahmen.isEmpty()) {

            String veranstaltungen = teilnahmen.stream()
                    .map(t -> "• " + t.getVeranstaltung().getName())
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining("\n"));

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.PERSON_CANNOT_BE_DELETED
                            + "\n\nVerwendet in:\n"
                            + veranstaltungen
            );
        }

        if (reisekostenabrechnungRepository.existsByFahrerId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.PERSON_USED_AS_FAHRER
            );
        }

        if (fahrtabschnittMitfahrerRepository.existsByPersonId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.PERSON_USED_AS_MITFAHRER
            );
        }

        personRepository.deleteById(id);
    }

    @Override
    public BulkDeleteResultDTO deletePersons(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.PERSON_NO_SELECTION
            );
        }

        List<Long> deletedIds = new java.util.ArrayList<>();
        List<BulkDeleteErrorDTO> errors = new java.util.ArrayList<>();

        for (Long id : ids) {
            try {
                deletePerson(id);
                deletedIds.add(id);

            } catch (ResponseStatusException e) {

                String message = e.getReason() != null
                        ? e.getReason()
                        : ErrorMessages.PERSON_CANNOT_BE_DELETED;

                errors.add(
                        new BulkDeleteErrorDTO(id, message)
                );
            }
        }

        return new BulkDeleteResultDTO(
                deletedIds,
                errors
        );
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
        Page<Person> page = personRepository
                .findAll(
                        PersonSpecification.byCriteria(criteria),
                        pageable
                );

        List<Person> persons = page.getContent();

        List<PersonListDTO> dtos = persons.stream()
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

        enrichListStatus(persons, dtos);

        return new org.springframework.data.domain.PageImpl<>(
                dtos,
                pageable,
                page.getTotalElements()
        );
    }

    private void enrichListStatus(
            List<Person> persons,
            List<PersonListDTO> dtos
    ) {
        if (persons.isEmpty()) {
            return;
        }

        List<Long> personIds = persons.stream()
                .map(Person::getId)
                .toList();

        var leiterIds =
                veranstaltungRepository.findLeiterPersonIds(personIds);

        var fahrerIds =
                reisekostenabrechnungRepository.findFahrerPersonIds(personIds);

        for (int i = 0; i < persons.size(); i++) {
            Person person = persons.get(i);
            PersonListDTO dto = dtos.get(i);

            var status = personDataStatusService.determineStatus(
                    person,
                    leiterIds.contains(person.getId()),
                    fahrerIds.contains(person.getId())
            );

            dto.setDataStatus(status.getStatus());
        }
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
                                        ErrorMessages.VEREIN_NOT_FOUND
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
                    ErrorMessages.PERSON_ONLY_ONE_HAUPTVEREIN
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
                            ErrorMessages.PERSON_ALREADY_EXISTS
                    );
                });
    }
    private void fillBankIfMissing(Person person) {

        if (person.getBankName() != null
                && !person.getBankName().isBlank()) {
            return;
        }

        if (person.getBic() == null
                || person.getBic().isBlank()) {
            return;
        }

        String bank =
                bankLookupService.findBankName(
                        person.getBic()
                );

        if (bank != null) {
            person.setBankName(bank);
        }
    }
}