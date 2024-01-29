package com.kcserver.service;


import com.kcserver.dto.PersonCreateDTO;
import com.kcserver.dto.PersonWithVereinDTO;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VereinRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;


@Service
public class PersonService {
    private final PersonRepository personRepository;
    private final VereinRepository vereinRepository;

    @Autowired
    public PersonService(PersonRepository personRepository, VereinRepository vereinRepository) {
        this.personRepository = personRepository;
        this.vereinRepository = vereinRepository;
    }

    public List<PersonWithVereinDTO> getAllPersonenWithVerein() {
        List<Person> persons = personRepository.findAll();
        List<PersonWithVereinDTO> personDTOs = new ArrayList<>();

        for (Person person : persons) {
            PersonWithVereinDTO dto = new PersonWithVereinDTO();
            dto.setPersonId(person.getId());
            dto.setPersonName(person.getVorname() + " " + person.getName());
            dto.setPersonTelefon(person.getTelefon());
            dto.setPersonStrasse(person.getStrasse());
            dto.setPersonPLZ(person.getPlz());
            dto.setPersonOrt(person.getOrt());
            dto.setPersonBank(person.getBankName());
            dto.setPersonIBAN(person.getIban());
            dto.setPersonBIC(person.getBic());
            if (!person.getMitglieder().isEmpty()) {
                dto.setVereinName(person.getMitglieder().iterator().next().getVereinMitgliedschaft().getName());
            }
            personDTOs.add(dto);
        }

        return personDTOs;
    }

    public List<Person> getAllPersonen() {
        return personRepository.findAll();
    }

    public Person getPersonByName(String name) {
        return personRepository.findByNameIs(name);
    }

    public List<Person> getPersonsByName(String name) {
        return personRepository.findByName(name);
    }

    public Person createPerson(Person person) {
        return personRepository.save(person);
    }

    public Person getPerson(@PathVariable long id) {
        return personRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Ungültige Person Id %s", id)));
    }

    public Person updatePerson(Long personId, Person updatedPerson) {
        Person existingPerson = personRepository.findById(personId).orElse(null);
        if (existingPerson != null) {
            existingPerson.setName(updatedPerson.getName());
            existingPerson.setVorname(updatedPerson.getVorname());
            existingPerson.setStrasse(updatedPerson.getStrasse());
            existingPerson.setPlz(updatedPerson.getPlz());
            existingPerson.setOrt(updatedPerson.getOrt());
            existingPerson.setTelefon(updatedPerson.getTelefon());
            existingPerson.setBankName(updatedPerson.getBankName());
            existingPerson.setIban(updatedPerson.getIban());
            existingPerson.setBic(updatedPerson.getBic());
            return personRepository.save(existingPerson);
        }
        return null;
    }

    public Person createPersonWithVerein(PersonCreateDTO personDTO) {
        Verein verein = vereinRepository.findById(personDTO.getVereinId()).orElseThrow(() -> new EntityNotFoundException("Verein not found"));

        Person person = new Person(personDTO.getName(), personDTO.getVorname(), personDTO.getStrasse(),
               personDTO.getPlz(), personDTO.getOrt(), personDTO.getTelefon(), personDTO.getBankName(),
                personDTO.getIban(), personDTO.getBic());

        personRepository.save(person);

        Mitglied mitglied = new Mitglied();
        mitglied.setPersonMitgliedschaft(person);
        mitglied.setVereinMitgliedschaft(verein);
        mitglied.setFunktion("Member"); // Set the appropriate function
        mitglied.setHauptVerein(true); // Set the HauptVerein flag if needed

        verein.addMitglied(mitglied);

        return person;
    }


    public boolean deletePerson(long id) {
        if (personRepository.existsById(id)) {
            personRepository.deleteById(id);
            return true;
        }
        return false;
    }


}
