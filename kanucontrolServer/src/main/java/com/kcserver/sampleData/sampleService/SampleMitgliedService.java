package com.kcserver.sampleData.sampleService;

import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.repository.MitgliedRepository;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VereinRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SampleMitgliedService {
    private static final Logger logger = LoggerFactory.getLogger(SamplePersonService.class);

    private final MitgliedRepository mitgliedRepository;
    private final PersonRepository personRepository;
    private final VereinRepository vereinRepository;

    public SampleMitgliedService(MitgliedRepository mitgliedRepository, PersonRepository personRepository,
                                 VereinRepository vereinRepository) {
        this.mitgliedRepository = mitgliedRepository;
        this.personRepository = personRepository;
        this.vereinRepository = vereinRepository;
    }
    public void initialize(){
        // Check if Mitglied data already exists
        if (mitgliedRepository.count() == 0) {
            logger.info("Loading sample Mitglied data...");

            // Retrieve an existing Person and Verein from the database
            Person existingPerson = personRepository.findByVornameAndName("Chris", "Schog");
            Verein existingVerein = vereinRepository.findByNameIs("Eschweiler Kanu Club");

            // Check if Person and Verein exist
            if (existingPerson != null && existingVerein != null) {
                logger.info("Person found: {} {}, ID: {}", existingPerson.getVorname(), existingPerson.getName(), existingPerson.getId());
                logger.info("Verein found: {}, ID: {}", existingVerein.getName(), existingVerein.getId());

                // Create a new Mitglied instance
                Mitglied mitglied = new Mitglied();
                mitglied.setPersonMitgliedschaft(existingPerson);
                mitglied.setVereinMitgliedschaft(existingVerein);
                mitglied.setFunktion("Geschäftsführer");
                mitglied.setHauptVerein(true);

                // Save the Mitglied instance
                mitgliedRepository.save(mitglied);
                logger.info("Sample Mitglied created: Person ID {}, Verein ID {}, Funktion {}",
                        mitglied.getPersonMitgliedschaft().getId(),
                        mitglied.getVereinMitgliedschaft().getId(),
                        mitglied.getFunktion());

                // Retrieve and log all HauptVerein members
                List<Mitglied> hauptVereinMembers = mitgliedRepository.findByHauptVerein(true);
                for (Mitglied member : hauptVereinMembers) {
                    logger.info("Member ID: {}, Person ID: {}, Verein ID: {}, Funktion: {}",
                            member.getId(),
                            member.getPersonMitgliedschaft().getId(),
                            member.getVereinMitgliedschaft().getId(),
                            member.getFunktion());
                }
            } else {
                logger.error("Required Person or Verein not found in the database. Sample Mitglied creation aborted.");
            }
        } else {
            logger.info("Sample Mitglied data already exists. Skipping initialization.");
        }
    }
}
