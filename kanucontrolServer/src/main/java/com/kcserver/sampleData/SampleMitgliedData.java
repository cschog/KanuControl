package com.kcserver.sampleData;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.entity.Mitglied;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VereinRepository;
import com.kcserver.repository.MitgliedRepository;
import com.kcserver.service.PersonService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;


@Configuration
@Order(3)
public class SampleMitgliedData {

    //private static final Logger logger = LoggerFactory.getLogger(SampleMitgliedData.class);

    @Bean
    public CommandLineRunner sampleMitglied(
            MitgliedRepository mitgliedRepository,
            PersonRepository personRepository,
            PersonService personService,
            VereinRepository vereinRepository) {


        return (args) -> {
            // Retrieve an existing Person and Verein from the database
            Person existPerson = personService.getPersonByName("Schog");
            Person existingPerson = personRepository.findByVornameAndName("Chris","Schog");
            Verein existingVerein = vereinRepository.findById(1L).orElse(null);

            System.out.println("existingPerson: " + existingPerson);
            System.out.println("existPerson: " + existPerson);
            System.out.println("existingVerein: " + existingVerein);


            //if (existingPerson != null && existingVerein != null) {
                // Create a new Mitglied instance
                Mitglied mitglied = new Mitglied();
                mitglied.setPersonMitgliedschaft(existingPerson);
                mitglied.setVereinMitgliedschaft(existingVerein);
                mitglied.setFunktion("Geschäftsführer");
                mitglied.setHauptVerein(true);

                // Save the Mitglied instance
                mitgliedRepository.save(mitglied);

                List<Mitglied> hauptVereinMembers = mitgliedRepository.findByHauptVerein(true);
                for (Mitglied member : hauptVereinMembers) {
                    System.out.println("Member ID: " + member.getId());
                    System.out.println("Funktion: " + member.getFunktion());
                    // Print other attributes if needed
                }

           // }
        };
    }
}

