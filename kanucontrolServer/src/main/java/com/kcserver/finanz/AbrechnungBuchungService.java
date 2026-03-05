package com.kcserver.finanz;

import com.kcserver.dto.abrechnung.AbrechnungBuchungCreateDTO;
import com.kcserver.dto.abrechnung.AbrechnungBuchungDTO;
import com.kcserver.entity.*;
import com.kcserver.enumtype.AbrechnungsStatus;
import com.kcserver.mapper.AbrechnungMapper;
import com.kcserver.repository.AbrechnungBuchungRepository;
import com.kcserver.repository.AbrechnungRepository;
import com.kcserver.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class AbrechnungBuchungService {

    private final AbrechnungRepository abrechnungRepository;
    private final AbrechnungBuchungRepository buchungRepository;
    private final PersonRepository personRepository;
    private final AbrechnungMapper mapper;

    /* =========================================================
       ADD
       ========================================================= */

    public AbrechnungBuchungDTO addBuchung(Long veranstaltungId,
                                           AbrechnungBuchungCreateDTO dto) {

        Abrechnung a = getAbrechnung(veranstaltungId);
        checkEditable(a);

        AbrechnungBuchung b = new AbrechnungBuchung();

        b.setKategorie(dto.getKategorie());
        b.setBetrag(dto.getBetrag());
        b.setDatum(dto.getDatum());
        b.setBeschreibung(dto.getBeschreibung());

        if (dto.getPersonId() != null) {
            Person person = getPerson(dto.getPersonId());
            validateKuerzel(person);
            b.setPerson(person);
        }

        a.addBuchung(b);

        buchungRepository.save(b);

        return mapper.toDTO(b);
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    public AbrechnungBuchungDTO updateBuchung(Long veranstaltungId,
                                              Long buchungId,
                                              AbrechnungBuchungCreateDTO dto) {

        AbrechnungBuchung b = buchungRepository.findById(buchungId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Buchung nicht gefunden"
                        ));

        validateVeranstaltung(veranstaltungId, b);

        checkEditable(b.getAbrechnung());

        b.setKategorie(dto.getKategorie());
        b.setBetrag(dto.getBetrag());
        b.setDatum(dto.getDatum());
        b.setBeschreibung(dto.getBeschreibung());

        if (dto.getPersonId() != null) {
            Person person = getPerson(dto.getPersonId());
            validateKuerzel(person);
            b.setPerson(person);
        } else {
            b.setPerson(null);
        }

        return mapper.toDTO(b);
    }

    /* =========================================================
       DELETE
       ========================================================= */

    public void deleteBuchung(Long veranstaltungId, Long buchungId) {

        AbrechnungBuchung b = buchungRepository.findById(buchungId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Buchung nicht gefunden"
                        ));

        validateVeranstaltung(veranstaltungId, b);

        Abrechnung a = b.getAbrechnung();

        checkEditable(a);

        a.removeBuchung(b);

        buchungRepository.delete(b);
    }

    /* ========================================================= */

    private Abrechnung getAbrechnung(Long veranstaltungId) {

        return abrechnungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Abrechnung nicht gefunden"
                        ));
    }

    private Person getPerson(Long personId) {
        return personRepository.findById(personId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Person nicht gefunden"
                        ));
    }

    private void validateVeranstaltung(Long veranstaltungId, AbrechnungBuchung b) {

        if (!b.getAbrechnung()
                .getVeranstaltung()
                .getId()
                .equals(veranstaltungId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Buchung gehört zu anderer Veranstaltung"
            );
        }
    }

    private void checkEditable(Abrechnung a) {

        if (a.getStatus() == AbrechnungsStatus.ABGESCHLOSSEN) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Abrechnung ist abgeschlossen und nicht mehr änderbar"
            );
        }
    }

    private void validateKuerzel(Person person) {

        if (person.getKuerzel() == null || person.getKuerzel().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Person hat kein Kürzel hinterlegt"
            );
        }
    }
}