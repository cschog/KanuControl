package com.kcserver.service;

import com.kcserver.entity.Person;
import com.kcserver.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PersonService {
    private final PersonRepository personRepository;
    //private final VereinRepository vereinRepository;

    @Autowired
    public PersonService(PersonRepository personRepository/*, VereinRepository vereinRepository*/) {
        this.personRepository = personRepository;
        //this.vereinRepository = vereinRepository;
    }


    public List<Person> getAllPersonen() {
        return personRepository.findAll();
    }

    public Person getPersonByName(String name) {
        return personRepository.findByNameIs(name);
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

    public boolean deletePerson(long id) {
        if (personRepository.existsById(id)) {
            personRepository.deleteById(id);
            return true;
        }
        return false;
    }


}
