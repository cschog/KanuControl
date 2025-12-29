package com.kcserver.service;

import com.kcserver.dto.VeranstaltungDTO;
import com.kcserver.entity.*;
import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.mapper.VeranstaltungMapper;
import com.kcserver.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class VeranstaltungServiceImpl implements VeranstaltungService {

    private final VeranstaltungRepository veranstaltungRepository;
    private final VereinRepository vereinRepository;
    private final PersonRepository personRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final VeranstaltungMapper veranstaltungMapper;

    public VeranstaltungServiceImpl(
            VeranstaltungRepository veranstaltungRepository,
            VereinRepository vereinRepository,
            PersonRepository personRepository,
            TeilnehmerRepository teilnehmerRepository,
            VeranstaltungMapper veranstaltungMapper
    ) {
        this.veranstaltungRepository = veranstaltungRepository;
        this.vereinRepository = vereinRepository;
        this.personRepository = personRepository;
        this.teilnehmerRepository = teilnehmerRepository;
        this.veranstaltungMapper = veranstaltungMapper;
    }

    /* =========================================================
       CREATE
       ========================================================= */

    @Override
    public VeranstaltungDTO create(VeranstaltungDTO dto) {

        if (veranstaltungRepository.existsByAktivTrue()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "There is already an active Veranstaltung"
            );
        }

        Verein verein = vereinRepository.findById(dto.getVereinId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Verein not found"
                ));

        Person leiter = personRepository.findById(dto.getLeiterId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Leiter not found"
                ));

        validateLeiterAge(leiter);

        Veranstaltung veranstaltung = new Veranstaltung();
        veranstaltung.setName(dto.getName());
        veranstaltung.setTyp(dto.getTyp());
        veranstaltung.setBeginnDatum(dto.getBeginnDatum());
        veranstaltung.setEndeDatum(dto.getEndeDatum());
        veranstaltung.setBeginnZeit(dto.getBeginnZeit());
        veranstaltung.setEndeZeit(dto.getEndeZeit());
        veranstaltung.setVerein(verein);
        veranstaltung.setLeiter(leiter);
        veranstaltung.setGeplanteTeilnehmerMaennlich(dto.getGeplanteTeilnehmerMaennlich());
        veranstaltung.setGeplanteTeilnehmerWeiblich(dto.getGeplanteTeilnehmerWeiblich());
        veranstaltung.setGeplanteTeilnehmerDivers(dto.getGeplanteTeilnehmerDivers());
        veranstaltung.setGeplanteMitarbeiterMaennlich(dto.getGeplanteMitarbeiterMaennlich());
        veranstaltung.setGeplanteMitarbeiterWeiblich(dto.getGeplanteMitarbeiterWeiblich());
        veranstaltung.setGeplanteMitarbeiterDivers(dto.getGeplanteMitarbeiterDivers());
        veranstaltung.setAktiv(true);

        Veranstaltung saved = veranstaltungRepository.save(veranstaltung);

        // 🔒 Leiter automatisch als Teilnehmer
        Teilnehmer leiterTeilnehmer = new Teilnehmer();
        leiterTeilnehmer.setVeranstaltung(saved);
        leiterTeilnehmer.setPerson(leiter);
        leiterTeilnehmer.setRolle(TeilnehmerRolle.LEITER);
        teilnehmerRepository.save(leiterTeilnehmer);

        return veranstaltungMapper.toDTO(saved);
    }

    /* =========================================================
       READ
       ========================================================= */

    @Override
    public List<VeranstaltungDTO> getAll() {
        return veranstaltungRepository.findAll()
                .stream()
                .map(veranstaltungMapper::toDTO)
                .toList();
    }

    @Override
    public VeranstaltungDTO getActive() {
        return veranstaltungRepository.findByAktivTrue()
                .map(veranstaltungMapper::toDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No active Veranstaltung"
                ));
    }

    @Override
    public VeranstaltungDTO getById(Long id) {
        return veranstaltungRepository.findById(id)
                .map(veranstaltungMapper::toDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Veranstaltung not found"
                ));
    }

    /* =========================================================
       STATE
       ========================================================= */

    @Override
    public void beenden() {
        Veranstaltung aktiv = veranstaltungRepository.findByAktivTrue()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No active Veranstaltung"
                ));

        aktiv.setAktiv(false);
        veranstaltungRepository.save(aktiv);
    }

    @Override
    public void aktivieren(Long id) {

        if (veranstaltungRepository.existsByAktivTrue()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another Veranstaltung is already active"
            );
        }

        Veranstaltung veranstaltung = veranstaltungRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Veranstaltung not found"
                ));

        veranstaltung.setAktiv(true);
        veranstaltungRepository.save(veranstaltung);
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private void validateLeiterAge(Person person) {
        if (person.getGeburtsdatum() == null ||
                person.getGeburtsdatum()
                        .plusYears(18)
                        .isAfter(LocalDate.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Leiter must be at least 18 years old"
            );
        }
    }
}