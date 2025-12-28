package com.kcserver.service;

import com.kcserver.dto.MitgliedDTO;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.mapper.EntityMapper;
import com.kcserver.repository.MitgliedRepository;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VereinRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
@Service
public class MitgliedService {

    private final MitgliedRepository mitgliedRepository;
    private final PersonRepository personRepository;
    private final VereinRepository vereinRepository;
    private final EntityMapper mapper;

    public MitgliedService(
            MitgliedRepository mitgliedRepository,
            PersonRepository personRepository,
            VereinRepository vereinRepository,
            EntityMapper mapper) {

        this.mitgliedRepository = mitgliedRepository;
        this.personRepository = personRepository;
        this.vereinRepository = vereinRepository;
        this.mapper = mapper;
    }

    @Transactional
    public MitgliedDTO createMitglied(MitgliedDTO dto) {

        Person person = personRepository.findById(dto.getPersonId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Person not found"));

        Verein verein = vereinRepository.findById(dto.getVereinId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Verein not found"));

        // Fachliche Regel: nur ein Hauptverein pro Person
        if (Boolean.TRUE.equals(dto.getHauptVerein())) {
            mitgliedRepository.findByPerson_IdAndHauptVereinTrue(dto.getPersonId())
                    .ifPresent(existing -> {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Person already has a Hauptverein"
                        );
                    });
        }

        Mitglied mitglied = new Mitglied();
        mitglied.setPerson(person);
        mitglied.setVerein(verein);
        mitglied.setFunktion(dto.getFunktion());     // ✅ Enum
        mitglied.setHauptVerein(dto.getHauptVerein());

        return mapper.toMitgliedDTO(mitgliedRepository.save(mitglied));
    }
}