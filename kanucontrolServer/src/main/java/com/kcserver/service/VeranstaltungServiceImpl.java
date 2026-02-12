package com.kcserver.service;

import com.kcserver.dto.veranstaltung.VeranstaltungCreateDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungDetailDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungListDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungUpdateDTO;
import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.entity.Verein;
import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.mapper.VeranstaltungMapper;
import com.kcserver.repository.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Slf4j
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
    @Transactional
    public VeranstaltungDetailDTO create(VeranstaltungCreateDTO dto) {

        // ⭐ GENAU WIE BEI MITGLIED
        veranstaltungRepository.unsetAktiveVeranstaltung();

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
        veranstaltung.setBeginnZeit(dto.getBeginnZeit());
        veranstaltung.setEndeDatum(dto.getEndeDatum());
        veranstaltung.setEndeZeit(dto.getEndeZeit());
        veranstaltung.setVerein(verein);
        veranstaltung.setLeiter(leiter);
        veranstaltung.setAktiv(true);

        Veranstaltung saved = veranstaltungRepository.save(veranstaltung);

        // Leiter MUSS exakt 1 Teilnehmer sein (Domain Invariant)
        teilnehmerRepository
                .findByVeranstaltungAndPerson(saved, leiter)
                .ifPresentOrElse(
                        existing -> {
                            existing.setRolle(TeilnehmerRolle.LEITER);
                            teilnehmerRepository.save(existing);
                        },
                        () -> {
                            Teilnehmer t = new Teilnehmer();
                            t.setVeranstaltung(saved);
                            t.setPerson(leiter);
                            t.setRolle(TeilnehmerRolle.LEITER);
                            teilnehmerRepository.save(t);
                        }
                );

        return veranstaltungMapper.toDetailDTO(
                veranstaltungRepository.findByIdWithRelations(saved.getId()).orElseThrow()
        );
    }

    /* =========================================================
       READ
       ========================================================= */

    @Override
    public VeranstaltungDetailDTO getById(Long id) {
        return veranstaltungMapper.toDetailDTO(
                veranstaltungRepository.findByIdWithRelations(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Veranstaltung not found"
                        ))
        );
    }

    @Override
    public VeranstaltungDetailDTO getActive() {
        return veranstaltungRepository.findByAktivTrue()
                .map(veranstaltungMapper::toDetailDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No active Veranstaltung"
                ));
    }

    @Override
    public Page<VeranstaltungListDTO> getAll(Pageable pageable) {
        return getAll(null, null, null, null, null, null, pageable);
    }

    @Override
    public Page<VeranstaltungListDTO> getAll(
            String name,
            Boolean aktiv,
            Long vereinId,
            LocalDate beginnVon,
            LocalDate beginnBis,
            VeranstaltungTyp typ,
            Pageable pageable
    ) {
        Specification<Veranstaltung> spec = null;

        if (name != null && !name.isBlank()) {
            spec = VeranstaltungSpecs.nameContains(name);
        }
        if (aktiv != null) {
            spec = (spec == null)
                    ? VeranstaltungSpecs.aktivEquals(aktiv)
                    : spec.and(VeranstaltungSpecs.aktivEquals(aktiv));
        }
        if (vereinId != null) {
            spec = (spec == null)
                    ? VeranstaltungSpecs.vereinEquals(vereinId)
                    : spec.and(VeranstaltungSpecs.vereinEquals(vereinId));
        }
        if (beginnVon != null) {
            spec = (spec == null)
                    ? VeranstaltungSpecs.beginnVon(beginnVon)
                    : spec.and(VeranstaltungSpecs.beginnVon(beginnVon));
        }
        if (beginnBis != null) {
            spec = (spec == null)
                    ? VeranstaltungSpecs.beginnBis(beginnBis)
                    : spec.and(VeranstaltungSpecs.beginnBis(beginnBis));
        }
        if (typ != null) {
            spec = (spec == null)
                    ? VeranstaltungSpecs.typEquals(typ)
                    : spec.and(VeranstaltungSpecs.typEquals(typ));
        }

        return veranstaltungRepository
                .findAll(spec, pageable)
                .map(veranstaltungMapper::toListDTO);
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    @Override
    public VeranstaltungDetailDTO update(
            Long id,
            VeranstaltungUpdateDTO dto
    ) {
        Veranstaltung veranstaltung = getVeranstaltungOrThrow(id);

        if (dto.getName() != null) {
            veranstaltung.setName(dto.getName());
        }
        if (dto.getTyp() != null) {
            veranstaltung.setTyp(dto.getTyp());
        }
        if (dto.getBeginnDatum() != null) {
            veranstaltung.setBeginnDatum(dto.getBeginnDatum());
        }
        if (dto.getBeginnZeit() != null) {
            veranstaltung.setBeginnZeit(dto.getBeginnZeit());
        }
        if (dto.getEndeDatum() != null) {
            veranstaltung.setEndeDatum(dto.getEndeDatum());
        }
        if (dto.getEndeZeit() != null) {
            veranstaltung.setEndeZeit(dto.getEndeZeit());
        }

        // -------- Leiter -------------------
        if (dto.getLeiterId() != null) {

            Person newLeiter = personRepository.findById(dto.getLeiterId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leiter not found"));

            validateLeiterAge(newLeiter);

            Person oldLeiter = veranstaltung.getLeiter();

            // 1️⃣ alten Leiter zurücksetzen
            teilnehmerRepository
                    .findByVeranstaltungAndPerson(veranstaltung, oldLeiter)
                    .ifPresent(t -> {
                        t.setRolle(null);
                        teilnehmerRepository.save(t);
                    });

            // 2️⃣ neuen Leiter als Teilnehmer holen/erzeugen
            Teilnehmer newLeiterTeilnehmer = teilnehmerRepository
                    .findByVeranstaltungAndPerson(veranstaltung, newLeiter)
                    .orElseGet(() -> {
                        Teilnehmer t = new Teilnehmer();
                        t.setVeranstaltung(veranstaltung);
                        t.setPerson(newLeiter);
                        t.setRolle(null);
                        return teilnehmerRepository.save(t);
                    });

            // 3️⃣ Rolle setzen
            newLeiterTeilnehmer.setRolle(TeilnehmerRolle.LEITER);
            teilnehmerRepository.save(newLeiterTeilnehmer);

            // 4️⃣ Veranstaltung updaten
            veranstaltung.setLeiter(newLeiter);
        }

        // ------- Verein -------------------

        if (dto.getVereinId() != null) {

            Verein newVerein = vereinRepository.findById(dto.getVereinId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verein not found"));

            veranstaltung.setVerein(newVerein);
        }



        return veranstaltungMapper.toDetailDTO(veranstaltung);
    }
    @Override
    @Transactional
    public VeranstaltungDetailDTO setActive(Long veranstaltungId) {

        log.info("➡️ setActive called for veranstaltungId={}", veranstaltungId);

        Veranstaltung neu = veranstaltungRepository.findById(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Veranstaltung not found"
                ));

        log.info("🔎 Before reset: id={}, aktiv={}", neu.getId(), neu.isAktiv());

        // 1️⃣ ALLE aktiven Veranstaltungen zurücksetzen
        veranstaltungRepository.unsetAktiveVeranstaltung();

        // 2️⃣ Entity explizit neu laden (WICHTIG!)
        Veranstaltung refreshed = veranstaltungRepository
                .findByIdWithRelations(veranstaltungId)
                .orElseThrow();

        log.info("🔄 After reload: id={}, aktiv={}", refreshed.getId(), refreshed.isAktiv());

        // 3️⃣ neue aktiv setzen
        refreshed.setAktiv(true);
        veranstaltungRepository.save(refreshed);

        log.info("✅ After save: id={}, aktiv={}", refreshed.getId(), refreshed.isAktiv());

        return veranstaltungMapper.toDetailDTO(refreshed);
    }
    /* =========================================================
       DELETE
       ========================================================= */
    @Override
    @Transactional
    public void delete(Long id) {

        Veranstaltung v = veranstaltungRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Veranstaltung not found"
                ));

        Long veranstaltungId = v.getId();
        Long leiterId = v.getLeiter().getId();

        // ❗ gibt es Teilnehmer außer Leiter?
        boolean hasOtherTeilnehmer =
                teilnehmerRepository.existsNichtLeiterTeilnehmer(
                        veranstaltungId,
                        TeilnehmerRolle.LEITER
                );

        if (hasOtherTeilnehmer) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Veranstaltung cannot be deleted while Teilnehmer exist"
            );
        }

        veranstaltungRepository.delete(v);
    }


    /* =========================================================
       HELPER
       ========================================================= */

    private Veranstaltung getVeranstaltungOrThrow(Long id) {
        return veranstaltungRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Veranstaltung not found"
                ));
    }


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