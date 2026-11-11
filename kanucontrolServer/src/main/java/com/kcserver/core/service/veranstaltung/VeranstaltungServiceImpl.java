package com.kcserver.core.service.veranstaltung;

import com.kcserver.core.dto.veranstaltung.VeranstaltungCreateDTO;
import com.kcserver.core.dto.veranstaltung.VeranstaltungDetailDTO;
import com.kcserver.core.dto.veranstaltung.VeranstaltungFilterDTO;
import com.kcserver.core.dto.veranstaltung.VeranstaltungListDTO;
import com.kcserver.core.dto.veranstaltung.VeranstaltungUpdateDTO;
import com.kcserver.core.entity.Teilnehmer;
import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.core.repository.TeilnehmerRepository;
import com.kcserver.core.repository.VeranstaltungRepository;
import com.kcserver.core.repository.VeranstaltungSpecs;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.core.dto.person.PersonListDTO;
import com.kcserver.core.entity.Person;
import com.kcserver.core.entity.Verein;
import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.core.mapper.PersonMapper;
import com.kcserver.core.repository.VereinRepository;
import com.kcserver.kjfp.entity.*;
import com.kcserver.kjfp.entity.beitraege.Beitragsstruktur;
import com.kcserver.kjfp.entity.beitraege.Zahlungsnachweis;
import com.kcserver.kjfp.entity.fahrkosten.Reisekostenabrechnung;
import com.kcserver.core.enumtype.TeilnehmerRolle;
import com.kcserver.core.mapper.VeranstaltungMapper;
import com.kcserver.kjfp.repository.*;
import com.kcserver.kjfp.repository.abrechnung.AbrechnungRepository;
import com.kcserver.kjfp.repository.beitrag.BeitragsstrukturRepository;
import com.kcserver.kjfp.repository.fahrkosten.ReisekostenabrechnungRepository;
import com.kcserver.kjfp.repository.zahlungsnachweis.ZahlungsnachweisRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import com.kcserver.kjfp.service.finanz.FinanzGruppeService;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class VeranstaltungServiceImpl implements VeranstaltungService {

    private final com.kcserver.kjfp.service.veranstaltung.KjfpVeranstaltungRegeln kjfpRegeln;
    private final VeranstaltungRepository veranstaltungRepository;
    private final VereinRepository vereinRepository;
    private final PersonRepository personRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final PlanungRepository planungRepository;
    private final VeranstaltungMapper veranstaltungMapper;
    private final PersonMapper personMapper;
    private final BeitragsstrukturRepository beitragsstrukturRepository;

    private final UnterkunftsartRepository unterkunftsartRepository;
    private final VerpflegungsmodellRepository verpflegungsmodellRepository;
    private final FinanzGruppeService finanzGruppeService;

    private final AbrechnungRepository abrechnungRepository;
    private final ZahlungsnachweisRepository zahlungsnachweisRepository;
    private final ReisekostenabrechnungRepository reisekostenabrechnungRepository;


    /* =========================================================
       CREATE
       ========================================================= */

    @Override
    public ApiResponse<VeranstaltungDetailDTO> create(
            VeranstaltungCreateDTO dto) {

        // =========================================================
        // 1. Zuerst alle Voraussetzungen prüfen
        // =========================================================

        Verein verein = vereinRepository.findById(dto.getVereinId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.VEREIN_NOT_FOUND
                ));

        boolean kjfpAktiv = !Boolean.FALSE.equals(dto.getKjfpAktiv());
        if (kjfpAktiv && (dto.getLeiterId() == null || dto.getBeginnZeit() == null || dto.getEndeZeit() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Für KJFP werden eine Leitung sowie Beginn- und Endzeit benötigt. FM oder JEM wird aus der Veranstaltungsdauer ermittelt.");
        }
        Person leiter = dto.getLeiterId() == null ? null : personRepository.findById(dto.getLeiterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.PERSON_NOT_FOUND));

        if (leiter != null && !leiter.isAktiv()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.VERANSTALTUNGSLEITER_INAKTIV);
        }

        if (kjfpAktiv) kjfpRegeln.validateLeiterAge(leiter);

        // =========================================================
        // 2. Erst wenn alle Voraussetzungen erfüllt sind:
        //    bisherige aktive Veranstaltung deaktivieren
        // =========================================================

        veranstaltungRepository.unsetAktiveVeranstaltung();
        veranstaltungRepository.flush();

        // =========================================================
        // 3. Veranstaltung erstellen
        // =========================================================

        Veranstaltung v = new Veranstaltung();
        v.setName(dto.getName());
        v.setTyp(dto.getTyp());
        v.setBeschreibung(dto.getBeschreibung());
        v.setBeginnDatum(dto.getBeginnDatum());
        v.setEndeDatum(dto.getEndeDatum());
        v.setBeginnZeit(dto.getBeginnZeit());
        v.setEndeZeit(dto.getEndeZeit());
        v.setVerein(verein);
        v.setLeiter(leiter);
        v.setAktiv(true);
        v.setKjfpAktiv(kjfpAktiv);
        v.setFahrtenAktiv(Boolean.TRUE.equals(dto.getFahrtenAktiv()));

        List<String> warnings = kjfpRegeln.savedKjfpWarnings(v);

        Veranstaltung saved = veranstaltungRepository.save(v);

        if (saved.isKjfpAktiv()) finanzGruppeService.getOrCreateVereinsFinanzGruppe(saved);

        if (saved.isKjfpAktiv()) {
        Teilnehmer t = new Teilnehmer();
        t.setVeranstaltung(saved);
        t.setPerson(leiter);
        t.setRolle(TeilnehmerRolle.LEITER);
        t.setKjfpZugeordnet(true);
        teilnehmerRepository.save(t);
        }

        Veranstaltung loaded = veranstaltungRepository
                .findByIdWithRelations(saved.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.VERANSTALTUNG_NOT_FOUND
                ));

        VeranstaltungDetailDTO dtoResult =
                veranstaltungMapper.toDetailDTO(loaded);

        return new ApiResponse<>(dtoResult, warnings);
    }

    /* =========================================================
       READ
       ========================================================= */

    @Override
    @Transactional(readOnly = true)
    public VeranstaltungDetailDTO getById(Long id) {
        return veranstaltungMapper.toDetailDTO(
                veranstaltungRepository.findByIdWithRelations(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<VeranstaltungListDTO> getAll() {
        return veranstaltungRepository.findAll()
                .stream()
                .map(veranstaltungMapper::toListDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VeranstaltungListDTO> search(
            VeranstaltungFilterDTO filter,
            Pageable pageable
    ) {
        return veranstaltungRepository.findAll(
                VeranstaltungSpecs.filter(filter),
                pageable
        ).map(veranstaltungMapper::toListDTO);
    }

    /* =========================================================
       AVAILABLE / ASSIGNED
       ========================================================= */

    @Override
    @Transactional(readOnly = true)
    public List<PersonListDTO> getAvailablePersons(Long veranstaltungId, String search) {

        checkVeranstaltungExists(veranstaltungId);

        String searchTerm = (search == null || search.isBlank()) ? "" : search.trim();

        return teilnehmerRepository
                .findAvailable(veranstaltungId, safe(searchTerm))
                .stream()
                .map(personMapper::toListDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonListDTO> getAssignedPersons(Long veranstaltungId) {

        return teilnehmerRepository
                .findAllKjfpWithPerson(veranstaltungId)
                .stream()
                .map(t -> personMapper.toListDTO(t.getPerson()))
                .toList();
    }


    @Transactional(readOnly = true)
    public List<VeranstaltungListDTO> searchAll(
            VeranstaltungFilterDTO filter
    ) {
        return veranstaltungRepository.findAll(
                        VeranstaltungSpecs.filter(filter)
                )
                .stream()
                .map(veranstaltungMapper::toListDTO)
                .toList();
    }

    /* =========================================================
       BULK
       ========================================================= */

    @Override
    public void addTeilnehmerBulk(Long veranstaltungId, List<Long> personIds) {

        Veranstaltung v = getVeranstaltungOrThrow(veranstaltungId);

        List<Person> persons = personRepository.findAllById(personIds);

        for (Person p : persons) {

            boolean exists = teilnehmerRepository
                    .findByVeranstaltungAndPerson(v, p)
                    .isPresent();

            if (!exists) {
                Teilnehmer t = new Teilnehmer();
                t.setVeranstaltung(v);
                t.setPerson(p);
                teilnehmerRepository.save(t);
            }
        }
    }

    @Override
    public void removeTeilnehmerBulk(Long veranstaltungId, List<Long> personIds) {

        Veranstaltung v = getVeranstaltungOrThrow(veranstaltungId);

        for (Long personId : personIds) {

            if (personIds.isEmpty()) return;

            Person p = personRepository.findById(personId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            teilnehmerRepository
                    .findByVeranstaltungAndPerson(v, p)
                    .ifPresent(t -> {

                        if (t.getRolle() == TeilnehmerRolle.LEITER) {
                            throw new ResponseStatusException(
                                    HttpStatus.CONFLICT,
                                    ErrorMessages.LEITER_KANN_NICHT_GELOESCHT_WERDEN
                            );
                        }

                        teilnehmerRepository.delete(t);
                    });
        }
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @Override
    public void delete(Long id) {

        Veranstaltung v = getVeranstaltungOrThrow(id);
        boolean warAktiv = v.isAktiv();

        // =========================================================
        // 1. Teilnehmer prüfen
        // =========================================================

        boolean hasTeilnehmer =
                teilnehmerRepository.existsNonLeiter(id, TeilnehmerRolle.LEITER);

        if (hasTeilnehmer) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.VERANSTALTUNG_CANNOT_BE_DELETED_WITH_TEILNEHMER
            );
        }

        // =========================================================
        // 2. Zahlungsnachweise löschen
        //
        //    Cascade:
        //    Zahlungspositionen
        //    Dokumente
        // =========================================================

        List<Zahlungsnachweis> zahlungsnachweise =
                zahlungsnachweisRepository
                        .findByVeranstaltungIdOrderByDatumDescIdDesc(id);

        zahlungsnachweisRepository.deleteAll(zahlungsnachweise);

        // =========================================================
        // 3. Abrechnung löschen
        //
        //    Cascade:
        //    AbrechnungBeleg
        //    AbrechnungBuchung
        //    Dokument
        //
        //    Wichtig:
        //    Dadurch verschwinden auch Referenzen von
        //    AbrechnungBuchung → Reisekostenabrechnung
        // =========================================================

        abrechnungRepository.findByVeranstaltungId(id)
                .ifPresent(abrechnungRepository::delete);

        // =========================================================
        // 4. Reisekostenabrechnungen löschen
        //
        //    Cascade:
        //    Fahrtabschnitt
        //    FahrtabschnittMitfahrer
        // =========================================================

        List<Reisekostenabrechnung> reisekosten =
                reisekostenabrechnungRepository
                        .findByVeranstaltungId(id);

        reisekostenabrechnungRepository.deleteAll(reisekosten);

        // =========================================================
        // 5. Planung löschen
        //
        //    Cascade:
        //    PlanungPosition
        // =========================================================

        planungRepository.findByVeranstaltungId(id)
                .ifPresent(planungRepository::delete);

        // =========================================================
        // 6. Teilnehmer löschen
        // =========================================================

        teilnehmerRepository.deleteByVeranstaltungId(id);

        // =========================================================
        // 7. Finanzgruppen löschen
        // =========================================================

        finanzGruppeService.deleteByVeranstaltungId(id);

        // =========================================================
        // 8. Veranstaltung löschen
        // =========================================================

        veranstaltungRepository.delete(v);
        veranstaltungRepository.flush();

        // =========================================================
        // 9. Falls aktive Veranstaltung gelöscht wurde:
        //    neueste Veranstaltung aktiv setzen
        // =========================================================

        if (warAktiv) {

            Optional<Veranstaltung> neuAktiv =
                    veranstaltungRepository
                            .findTopByIdNotOrderByBeginnDatumDescBeginnZeitDesc(id);

            neuAktiv.ifPresent(veranstaltung -> {
                veranstaltung.setAktiv(true);
                veranstaltungRepository.save(veranstaltung);
            });
        }
    }


    /* =========================================================
       HELPER
       ========================================================= */

    private Veranstaltung getVeranstaltungOrThrow(Long id) {
        return veranstaltungRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void checkVeranstaltungExists(Long id) {
        if (!veranstaltungRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ErrorMessages.VERANSTALTUNG_NOT_FOUND
            );
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }


    @Override
    @Transactional(readOnly = true)
    public VeranstaltungDetailDTO getActive() {
        return veranstaltungRepository.findByAktivTrue()
                .map(veranstaltungMapper::toDetailDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.NO_ACTIVE_VERANSTALTUNG
                ));
    }
    @Override
    @Transactional(readOnly = true)
    public Optional<VeranstaltungDetailDTO> getActiveOptional() {
        return veranstaltungRepository.findByAktivTrue()
                .map(veranstaltungMapper::toDetailDTO);
    }

    @Override
    public VeranstaltungDetailDTO setActive(Long veranstaltungId) {

        Veranstaltung neu = veranstaltungRepository.findById(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // alle deaktivieren
        veranstaltungRepository.unsetAktiveVeranstaltung();
        veranstaltungRepository.flush();

        // aktiv setzen
        neu.setAktiv(true);
        veranstaltungRepository.save(neu);

        // 🔥 ENTSCHEIDEND: neu laden mit JOIN FETCH
        Veranstaltung loaded = veranstaltungRepository
                .findByIdWithRelations(neu.getId())
                .orElseThrow();

        return veranstaltungMapper.toDetailDTO(loaded);
    }


    @Transactional
    @Override
    public ApiResponse<VeranstaltungDetailDTO> update(
            Long id,
            VeranstaltungUpdateDTO dto) {

        Veranstaltung v = veranstaltungRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, ErrorMessages.VERANSTALTUNG_NOT_FOUND
                ));

    /* =========================
       SIMPLE FIELDS
       ========================= */

        if (dto.getName() != null) {
            v.setName(dto.getName());
        }

        if (dto.getTyp() != null) {
            v.setTyp(dto.getTyp());
        }
        if (dto.getBeschreibung() != null) v.setBeschreibung(dto.getBeschreibung());
        if (dto.getKjfpAktiv() != null) v.setKjfpAktiv(dto.getKjfpAktiv());
        if (dto.getFahrtenAktiv() != null) v.setFahrtenAktiv(dto.getFahrtenAktiv());

        if (dto.getBeginnDatum() != null) {
            v.setBeginnDatum(dto.getBeginnDatum());
        }

        if (dto.getEndeDatum() != null) {
            v.setEndeDatum(dto.getEndeDatum());
        }

        if (dto.getBeginnZeit() != null) {
            v.setBeginnZeit(dto.getBeginnZeit());
        }

        if (dto.getEndeZeit() != null) {
            v.setEndeZeit(dto.getEndeZeit());
        }

    /* =========================
       VEREIN
       ========================= */

        if (dto.getVereinId() != null) {

            Verein verein = vereinRepository.findById(dto.getVereinId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, ErrorMessages.VEREIN_NOT_FOUND
                    ));

            v.setVerein(verein);
        }

/* =========================
   LEITER (inkl. Validierung)
   ========================= */

        if (dto.getLeiterId() != null) {

            Person leiter = personRepository.findById(dto.getLeiterId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            ErrorMessages.PERSON_NOT_FOUND
                    ));

            if (!leiter.isAktiv()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        ErrorMessages.VERANSTALTUNGSLEITER_INAKTIV
                );
            }

            if (v.isKjfpAktiv()) kjfpRegeln.validateLeiterAge(leiter);

            // Alten Leiter entfernen
            teilnehmerRepository
                    .findByVeranstaltungAndRolle(v, TeilnehmerRolle.LEITER)
                    .ifPresent(alterLeiter -> {

                        if (!alterLeiter.getPerson().getId().equals(leiter.getId())) {
                            alterLeiter.setRolle(null);
                            teilnehmerRepository.save(alterLeiter);
                        }
                    });

            // Neuen Leiter als Teilnehmer suchen oder anlegen
            Teilnehmer neuerLeiter =
                    teilnehmerRepository
                            .findByVeranstaltungAndPerson(v, leiter)
                            .orElseGet(() -> {

                                Teilnehmer t = new Teilnehmer();
                                t.setVeranstaltung(v);
                                t.setPerson(leiter);
                                return t;
                            });

            neuerLeiter.setRolle(TeilnehmerRolle.LEITER);
            neuerLeiter.setKjfpZugeordnet(v.isKjfpAktiv());

            teilnehmerRepository.save(neuerLeiter);

            // Veranstaltung ebenfalls aktualisieren
            v.setLeiter(leiter);
        }

        /* =========================
   DETAILFELDER
   ========================= */

        v.setCountryCode(dto.getCountryCode());
        v.setPlz(dto.getPlz());
        v.setOrt(dto.getOrt());

        v.setUnterkunftsart(
                getUnterkunftsart(dto.getUnterkunftsartId()));

        v.setVerpflegungsmodell(
                getVerpflegungsmodell(dto.getVerpflegungsmodellId()));

/* =========================
   GEBÜHREN
   ========================= */

        if (dto.getIndividuelleGebuehren() != null) {
            v.setIndividuelleGebuehren(dto.getIndividuelleGebuehren());
        }

/* =========================
   BEITRAGSSTRUKTUR
   ========================= */

        if (dto.getBeitragsstrukturId() != null) {

            Beitragsstruktur struktur =
                    beitragsstrukturRepository
                            .findById(dto.getBeitragsstrukturId())
                            .orElseThrow(() ->
                                    new EntityNotFoundException(
                                            "Beitragsstruktur nicht gefunden"
                                    )
                            );

            v.setBeitragsstruktur(struktur);
        }

/* =========================================
   VALIDIERUNG INDIVIDUELLE GEBÜHREN
   ========================================= */

        if (v.isIndividuelleGebuehren()) {


            // Struktur zwingend erforderlich
            if (v.getBeitragsstruktur() == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        ErrorMessages.INDIVIDUELLE_GEBUEHREN_REQUIRE_BEITRAGSSTRUKTUR
                );
            }

        } else {

            // Nur bei NICHT individuellen Gebühren setzen
            v.setStandardGebuehr(dto.getStandardGebuehr());
        }

        if (dto.getScope() != null) {
            v.setScope(dto.getScope());
        }

        List<String> warnings = kjfpRegeln.savedKjfpWarnings(v);

        if (v.isKjfpAktiv()) {
            if (v.getTyp() == null || v.getLeiter() == null || v.getBeginnZeit() == null || v.getEndeZeit() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Für KJFP werden eine Leitung sowie Beginn- und Endzeit benötigt. FM oder JEM wird aus der Veranstaltungsdauer ermittelt.");
            }
            kjfpRegeln.validateLeiterAge(v.getLeiter());
        }

        Veranstaltung saved = veranstaltungRepository.save(v);

        if (saved.isKjfpAktiv()) finanzGruppeService.getOrCreateVereinsFinanzGruppe(saved);

        VeranstaltungDetailDTO dtoResult =
                veranstaltungMapper.toDetailDTO(saved);

        return new ApiResponse<>(dtoResult, warnings);
    }

    @Override
    @Transactional(readOnly = true)
    public Veranstaltung findEntityById(Long id) {

        return veranstaltungRepository
                .findByIdWithRelations(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                ErrorMessages.VERANSTALTUNG_NOT_FOUND
                        )
                );
    }


    private Unterkunftsart getUnterkunftsart(Long id) {

        if (id == null) {
            return null;
        }

        return unterkunftsartRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.UNTERKUNFTSART_NOT_FOUND
                ));
    }

    private Verpflegungsmodell getVerpflegungsmodell(Long id) {

        if (id == null) {
            return null;
        }

        return verpflegungsmodellRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.VERPFLEGUNGSMODELL_NOT_FOUND
                ));
    }
}
