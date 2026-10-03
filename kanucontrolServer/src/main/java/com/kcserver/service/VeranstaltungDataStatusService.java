package com.kcserver.service;

import com.kcserver.dto.person.PersonDataStatusDTO;
import com.kcserver.dto.validation.DataFieldStatusDTO;
import com.kcserver.dto.validation.DataStatus;
import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.service.person.PersonDataStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VeranstaltungDataStatusService {

    private final TeilnehmerRepository teilnehmerRepository;
    private final PersonDataStatusService personDataStatusService;

    /**
     * Liefert die Personen, für die im Kontext dieser
     * Veranstaltung kein gültiges eFZ vorliegt.
     *
     * Die eigentliche eFZ-Prüfung erfolgt zentral im
     * PersonDataStatusService.
     */
    public List<String> getPersonenOhneGueltigesEfz(
            Veranstaltung veranstaltung
    ) {

        List<String> personen = new ArrayList<>();

        if (veranstaltung == null
                || veranstaltung.getId() == null
                || veranstaltung.getBeginnDatum() == null) {
            return personen;
        }

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository.findAllWithPerson(
                        veranstaltung.getId()
                );

        for (Teilnehmer t : teilnehmer) {

            if (t == null || t.getPerson() == null) {
                continue;
            }

            Person person = t.getPerson();

            boolean isLeiter =
                    veranstaltung.getLeiter() != null
                            && veranstaltung.getLeiter().getId()
                            .equals(person.getId());

            PersonDataStatusDTO status =
                    personDataStatusService.determineStatus(
                            person,
                            isLeiter,
                            false,
                            true,
                            t.getRolle(),
                            veranstaltung.getBeginnDatum()
                    );

            DataFieldStatusDTO efzStatus =
                    status.getFields().get("efz");

            if (efzStatus != null
                    && efzStatus.getStatus() == DataStatus.WARNING) {

                personen.add(formatName(person));
            }
        }

        return personen;
    }

    private String formatName(Person person) {

        String vorname = person.getVorname() != null
                ? person.getVorname().trim()
                : "";

        String nachname = person.getName() != null
                ? person.getName().trim()
                : "";

        return (vorname + " " + nachname).trim();
    }
}