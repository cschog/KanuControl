package com.kcserver.core.service;

import com.kcserver.core.dto.teilnehmer.TeilnehmerDetailDTO;
import com.kcserver.core.dto.teilnehmer.TeilnehmerKurzDTO;
import com.kcserver.core.dto.teilnehmer.TeilnehmerListDTO;
import com.kcserver.core.dto.teilnehmer.TeilnehmerRefDTO;
import com.kcserver.core.dto.teilnehmer.TeilnehmerSearchCriteria;
import com.kcserver.core.dto.teilnehmer.TeilnehmerUpdateDTO;
import com.kcserver.kjfp.service.AltersService;


import com.kcserver.core.dto.person.PersonDataStatusDTO;
import com.kcserver.core.dto.person.PersonListDTO;
import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.core.mapper.PersonMapper;
import com.kcserver.core.entity.Person;
import com.kcserver.core.entity.Teilnehmer;
import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.core.enumtype.TeilnehmerRolle;
import com.kcserver.core.mapper.TeilnehmerMapper;
import com.kcserver.core.repository.MitgliedRepository;
import com.kcserver.kjfp.repository.PersonRepository;

import com.kcserver.core.repository.VeranstaltungRepository;
import com.kcserver.kjfp.repository.fahrkosten.ReisekostenabrechnungRepository;
import com.kcserver.core.service.person.PersonDataStatusService;
import com.kcserver.core.specification.TeilnehmerSpecification;
import com.kcserver.core.repository.TeilnehmerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.kcserver.kjfp.repository.zahlungsnachweis.ZahlungsnachweisRepository;
import com.kcserver.kjfp.repository.abrechnung.AbrechnungBuchungRepository;
import com.kcserver.fahrten.repository.FahrtBesatzungRepository;


import java.util.List;

import static com.kcserver.core.exception.EntityFinder.getOr404;

import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Transactional
public class TeilnehmerService {

    private final TeilnehmerRepository teilnehmerRepository;
    private final VeranstaltungRepository veranstaltungRepository;
    private final PersonRepository personRepository;
    private final TeilnehmerMapper teilnehmerMapper;
    private final PersonMapper personMapper;
    private final ReisekostenabrechnungRepository reisekostenabrechnungRepository;
    private final ZahlungsnachweisRepository zahlungsnachweisRepository;
    private final AbrechnungBuchungRepository abrechnungBuchungRepository;
    private final FahrtBesatzungRepository fahrtBesatzungRepository;
    private final PersonDataStatusService personDataStatusService;
    private final AltersService altersService;


    /* =========================================================
       ADD SINGLE
       ========================================================= */



    public TeilnehmerDetailDTO addAllgemein(Long veranstaltungId, Long personId) {
        Veranstaltung veranstaltung = veranstaltungRepository.findByIdWithRelations(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.VERANSTALTUNG_NOT_FOUND));
        Person person = getPerson(personId);
        if (teilnehmerRepository.findByVeranstaltungAndPerson(veranstaltung, person).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessages.TEILNEHMER_ALREADY_EXISTS);
        Teilnehmer t = new Teilnehmer();
        t.setVeranstaltung(veranstaltung);
        t.setPerson(person);
        t.setKjfpZugeordnet(false);
        return teilnehmerMapper.toDetailDTO(teilnehmerRepository.save(t));
    }

    public void addAllgemeinBulk(Long veranstaltungId, List<Long> personIds) {
        Veranstaltung veranstaltung = veranstaltungRepository.findByIdWithRelations(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.VERANSTALTUNG_NOT_FOUND));
        for (Long personId : personIds) {
            Person person = getPerson(personId);
            if (teilnehmerRepository.findByVeranstaltungAndPerson(veranstaltung, person).isEmpty()) {
                Teilnehmer t = new Teilnehmer();
                t.setVeranstaltung(veranstaltung);
                t.setPerson(person);
                t.setKjfpZugeordnet(false);
                teilnehmerRepository.save(t);
            }
        }
    }

    public List<TeilnehmerDetailDTO> findAllGemeinsam(Long veranstaltungId) {
        return teilnehmerRepository.findAllWithPerson(veranstaltungId).stream().map(teilnehmerMapper::toDetailDTO).toList();
    }


    /* =========================================================
       ADD BULK
       ========================================================= */


    /* =========================================================
     UPDATE
     ========================================================= */
    public TeilnehmerDetailDTO update(
            Long veranstaltungId,
            Long teilnehmerId,
            TeilnehmerUpdateDTO dto
    ) {

        Teilnehmer t = teilnehmerRepository.findById(teilnehmerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!t.getVeranstaltung().getId().equals(veranstaltungId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.TEILNEHMER_IN_VERANSTALTUNG_NOT_FOUND
            );
        }

        if (dto.getRolle() != null) {

            // Leiter darf nicht überschrieben werden
            if (t.getRolle() == TeilnehmerRolle.LEITER) {
                return teilnehmerMapper.toDetailDTO(t);
            }

            if (dto.getRolle() == TeilnehmerRolle.LEITER) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        ErrorMessages.LEITER_MUSS_UEBER_VERANSTALTUNG_GESETZT_WERDEN
                );
            }

            t.setRolle(dto.getRolle());
        }

        return teilnehmerMapper.toDetailDTO(t);
    }

    /* =========================================================
       UPDATE ROLE
       ========================================================= */

    public void updateRolle(Long veranstaltungId, Long personId, TeilnehmerRolle rolle) {

        Teilnehmer t = teilnehmerRepository
                .findByVeranstaltungIdAndPersonId(veranstaltungId, personId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, ErrorMessages.TEILNEHMER_NOT_FOUND
                ));

        // ❗ Leiter darf NICHT überschrieben werden
        if (t.getRolle() == TeilnehmerRolle.LEITER) {
            return;
        }

        // nur null oder MITARBEITER erlaubt
        if (rolle == TeilnehmerRolle.LEITER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.LEITER_MUSS_UEBER_VERANSTALTUNG_GESETZT_WERDEN
            );
        }

        t.setRolle(rolle);   // null oder MITARBEITER
    }

    /* =========================================================
       DELETE SINGLE
       ========================================================= */

    public void removeTeilnehmer(Long veranstaltungId, Long teilnehmerId) {

        Teilnehmer teilnehmer = teilnehmerRepository.findById(teilnehmerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, ErrorMessages.TEILNEHMER_NOT_FOUND
                ));

        if (!teilnehmer.getVeranstaltung().getId().equals(veranstaltungId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, ErrorMessages.TEILNEHMER_NOT_IN_VERANSTALTUNG
            );
        }

        if (teilnehmer.getRolle() == TeilnehmerRolle.LEITER) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.LEITER_DARF_NICHT_ENTFERNT_WERDEN
            );
        }

        validateTeilnehmerKannEntferntWerden(
                veranstaltungId,
                teilnehmer.getPerson().getId()
        );

        teilnehmerRepository.delete(teilnehmer);
    }

    /* =========================================================
       DELETE BULK
       ========================================================= */

    public void removeTeilnehmerBulk(Long veranstaltungId, List<Long> personIds) {

        Veranstaltung v =
                getOr404(
                        veranstaltungRepository.findById(veranstaltungId),
                        ErrorMessages.VERANSTALTUNG_NOT_FOUND
                );

        Long leiterId = v.getLeiter() == null ? null : v.getLeiter().getId();

        List<Long> filteredIds = personIds.stream()
                .filter(id -> !id.equals(leiterId))
                .toList();

        if (filteredIds.isEmpty()) return;

        validateTeilnehmerKoennenEntferntWerden(
                veranstaltungId,
                filteredIds
        );

        teilnehmerRepository.deleteByVeranstaltungIdAndPersonIds(
                veranstaltungId,
                filteredIds
        );
    }

    /* =========================================================
     SEARCH (UI LIST)
     ========================================================= */
    @Transactional(readOnly = true)
    public List<TeilnehmerListDTO> search(Long veranstaltungId, String search) {
        return teilnehmerRepository.search(veranstaltungId, search)
                .stream()
                .map(teilnehmerMapper::toListDTO)
                .toList();
    }

    /* =========================================================
       SEARCH REF (Autocomplete)
       ========================================================= */
    @Transactional(readOnly = true)
    public List<TeilnehmerRefDTO> searchRef(Long veranstaltungId, String search) {
        return teilnehmerRepository.searchRef(veranstaltungId, search)
                .stream()
                .map(teilnehmerMapper::toRefDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public long count(Long veranstaltungId) {
        return teilnehmerRepository.countByVeranstaltungId(veranstaltungId);
    }

    @Transactional(readOnly = true)
    public List<TeilnehmerRefDTO> searchOhneFinanzgruppe(
            Long veranstaltungId,
            String search
    ) {
        return teilnehmerRepository.searchOhneFinanzgruppe(veranstaltungId, search)
                .stream()
                .map(teilnehmerMapper::toRefDTO)
                .toList();
    }

    /* =========================================================
       AVAILABLE
       ========================================================= */
    @Transactional(readOnly = true)
    public List<PersonListDTO> findAvailable(
            Long veranstaltungId,
            String search
    ) {

        return teilnehmerRepository.findAvailable(veranstaltungId, search)
                .stream()
                .map(person -> {

                    PersonListDTO dto =
                            personMapper.toListDTO(person);

                    PersonDataStatusDTO status =
                            determinePersonStatus(
                                    veranstaltungId,
                                    person,
                                    true
                            );

                    dto.setDataStatus(status.getStatus());

                    return dto;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PersonListDTO> findAvailable(
            Long veranstaltungId,
            String search,
            String verein,
            Boolean aktiv,
            Pageable pageable
    ) {

        return teilnehmerRepository
                .findAvailable(
                        veranstaltungId,
                        search,
                        verein,
                        aktiv,
                        pageable
                )
                .map(person -> {

                    PersonListDTO dto =
                            personMapper.toListDTO(person);

                    PersonDataStatusDTO status =
                            determinePersonStatus(
                                    veranstaltungId,
                                    person,
                                    false
                            );

                    dto.setDataStatus(status.getStatus());

                    return dto;
                });
    }

    /* =========================================================
       ASSIGNED
       ========================================================= */
    @Transactional(readOnly = true)
    public List<PersonListDTO> getAssigned(Long veranstaltungId) {

        Veranstaltung veranstaltung =
                veranstaltungRepository
                        .findById(veranstaltungId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                ErrorMessages.VERANSTALTUNG_NOT_FOUND
                        ));

        return teilnehmerRepository.findAllKjfpWithPerson(veranstaltungId)
                .stream()
                .map(t -> {

                    Person person = t.getPerson();

                    PersonListDTO dto =
                            personMapper.toListDTO(person);

                    boolean isLeiter =
                            t.getRolle() == TeilnehmerRolle.LEITER;

                    boolean isFahrer =
                            reisekostenabrechnungRepository
                                    .existsByVeranstaltungIdAndFahrerId(
                                            veranstaltungId,
                                            person.getId()
                                    );

                    PersonDataStatusDTO status =
                            personDataStatusService.determineStatus(
                                    person,
                                    isLeiter,
                                    isFahrer,
                                    true,
                                    t.getRolle(),
                                    veranstaltung.getBeginnDatum()
                            );

                    dto.setDataStatus(status.getStatus());

                    return dto;
                })
                .toList();
    }

    private PersonDataStatusDTO determinePersonStatus(
            Long veranstaltungId,
            Person person,
            boolean isTeilnehmer
    ) {
        boolean isLeiter =
                veranstaltungRepository.existsByIdAndLeiterId(
                        veranstaltungId,
                        person.getId()
                );

        boolean isFahrer =
                reisekostenabrechnungRepository
                        .existsByVeranstaltungIdAndFahrerId(
                                veranstaltungId,
                                person.getId()
                        );

        if (isTeilnehmer) {

            Teilnehmer teilnehmer =
                    teilnehmerRepository
                            .findByVeranstaltungIdAndPersonId(
                                    veranstaltungId,
                                    person.getId()
                            )
                            .orElse(null);

            Veranstaltung veranstaltung =
                    veranstaltungRepository
                            .findById(veranstaltungId)
                            .orElse(null);

            if (teilnehmer != null
                    && veranstaltung != null
                    && veranstaltung.getBeginnDatum() != null) {

                return personDataStatusService.determineStatus(
                        person,
                        isLeiter,
                        isFahrer,
                        true,
                        teilnehmer.getRolle(),
                        veranstaltung.getBeginnDatum()
                );
            }
        }

        return personDataStatusService.determineStatus(
                person,
                isLeiter,
                isFahrer,
                isTeilnehmer
        );
    }

    @Transactional(readOnly = true)
    public Page<TeilnehmerListDTO> getAssigned(
            Long veranstaltungId,
            TeilnehmerSearchCriteria criteria,
            Pageable pageable
    ) {
        Specification<Teilnehmer> spec =
                TeilnehmerSpecification.byCriteria(criteria)
                        .and((root, query, cb) -> cb.isTrue(root.get("kjfpZugeordnet")))
                        .and((root, query, cb) ->
                                cb.equal(
                                        root.get("veranstaltung").get("id"),
                                        veranstaltungId
                                )
                        );

        return teilnehmerRepository
                .findAll(spec, pageable)
                .map(teilnehmer -> {

                    TeilnehmerListDTO dto =
                            teilnehmerMapper.toListDTO(teilnehmer);

                    Person person = teilnehmer.getPerson();

                    PersonDataStatusDTO status =
                            determinePersonStatus(
                                    veranstaltungId,
                                    person,
                                    true
                            );

                    if (dto.getPerson() != null) {
                        dto.getPerson().setDataStatus(status.getStatus());
                    }

                    return dto;
                });
    }
    /* =========================================================
       SET LEITER
       ========================================================= */

    public TeilnehmerDetailDTO setLeiter(Long veranstaltungId, Long personId) {

        Veranstaltung veranstaltung =
                getOr404(
                        veranstaltungRepository.findById(veranstaltungId),
                        ErrorMessages.VERANSTALTUNG_NOT_FOUND
                );

        Person neuerLeiter = getPerson(personId);

        validateLeiterAge(neuerLeiter, veranstaltung);

        // Alle bisherigen Leiterrollen entfernen
        List<Teilnehmer> teilnehmer =
                teilnehmerRepository.findAllWithPerson(veranstaltungId);

        for (Teilnehmer t : teilnehmer) {
            if (t.getRolle() == TeilnehmerRolle.LEITER
                    && !t.getPerson().getId().equals(personId)) {

                t.setRolle(null);
                teilnehmerRepository.saveAndFlush(t);
            }
        }

        // Neuen Leiter als Teilnehmer suchen
        Teilnehmer neuerTeilnehmer =
                teilnehmerRepository
                        .findByVeranstaltungIdAndPersonId(
                                veranstaltungId,
                                personId
                        )
                        .orElseGet(() -> {

                            Teilnehmer t = new Teilnehmer();
                            t.setVeranstaltung(veranstaltung);
                            t.setPerson(neuerLeiter);

                            return t;
                        });

        // Neuer Leiter bekommt LEITER
        neuerTeilnehmer.setRolle(TeilnehmerRolle.LEITER);

        // Speichern
        Teilnehmer gespeichert =
                teilnehmerRepository.saveAndFlush(neuerTeilnehmer);

        // Veranstaltung ebenfalls aktualisieren
        veranstaltung.setLeiter(neuerLeiter);
        veranstaltungRepository.saveAndFlush(veranstaltung);

        return teilnehmerMapper.toDetailDTO(gespeichert);
    }

    private void validateLeiterAge(
            Person person,
            Veranstaltung veranstaltung
    ) {
        if (person.getGeburtsdatum() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.GEBURTSDATUM_REQUIRED_FOR_LEITER
            );
        }

        Integer alter =
                altersService.berechneAlterBeiBeginn(
                        person.getGeburtsdatum(),
                        veranstaltung.getBeginnDatum()
                );

        if (alter == null || alter < 18) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.LEITER_MIND_ALTER
            );
        }
    }


    /* =========================================================
       HELPER
       ========================================================= */

    private Person getPerson(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, ErrorMessages.PERSON_NOT_FOUND
                ));
    }


    @Transactional(readOnly = true)
    public List<TeilnehmerKurzDTO> findOhneKuerzel(Long veranstaltungId) {

        return teilnehmerRepository
                .findByVeranstaltungIdAndFinanzGruppeIsNull(veranstaltungId)
                .stream()
                .map(teilnehmerMapper::toKurzDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeilnehmerDetailDTO> findAllDetails(Long veranstaltungId) {

        return teilnehmerRepository
                .findAllKjfpWithPerson(veranstaltungId)
                .stream()
                .map(teilnehmerMapper::toDetailDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeilnehmerDetailDTO> findAllKjfpDetails(Long veranstaltungId) {
        return teilnehmerRepository.findAllKjfpWithPerson(veranstaltungId)
                .stream().map(teilnehmerMapper::toDetailDTO).toList();
    }
    private void validateTeilnehmerKannEntferntWerden(
            Long veranstaltungId,
            Long personId
    ) {
        if (reisekostenabrechnungRepository
                .existsByVeranstaltungAndPersonVerwendet(
                        veranstaltungId,
                        personId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.TEILNEHMER_USED_IN_REISEKOSTEN
            );
        }

        Teilnehmer teilnehmer = teilnehmerRepository
                .findByVeranstaltungIdAndPersonId(veranstaltungId, personId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.TEILNEHMER_NOT_FOUND
                ));

        if (zahlungsnachweisRepository
                .existsByPositionenTeilnehmerId(teilnehmer.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.TEILNEHMER_HAS_ZAHLUNGSNACHWEISE
            );
        }
        if (fahrtBesatzungRepository.existsByTeilnehmer_Id(teilnehmer.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Der Teilnehmer hat bestätigte Fahrtteilnahmen und kann nicht aus der Veranstaltung entfernt werden.");
        }
    }
    private void validateTeilnehmerKoennenEntferntWerden(
            Long veranstaltungId,
            List<Long> personIds
    ) {
        for (Long personId : personIds) {
            validateTeilnehmerKannEntferntWerden(
                    veranstaltungId,
                    personId
            );
        }
    }


}
