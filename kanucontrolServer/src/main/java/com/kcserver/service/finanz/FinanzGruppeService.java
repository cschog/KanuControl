package com.kcserver.service.finanz;

import com.kcserver.entity.FinanzGruppe;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.exception.BusinessRuleViolationException;
import com.kcserver.exception.ErrorMessages;
import com.kcserver.repository.abrechnung.AbrechnungBelegRepository;
import com.kcserver.repository.finanz.FinanzGruppeRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static com.kcserver.exception.ErrorMessages.*;


import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FinanzGruppeService {

    private final FinanzGruppeRepository repository;
    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final AbrechnungBelegRepository belegRepository;
    private final FinanzGruppeRepository finanzGruppeRepository;

    private static final String VK_KUERZEL = "VK";

    private boolean isVereinsFinanzGruppe(FinanzGruppe gruppe) {
        return VK_KUERZEL.equals(gruppe.getKuerzel());
    }

    private void checkNotSystem(FinanzGruppe gruppe) {

        if (gruppe.isSystem()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    FINANZGRUPPE_SYSTEM_NOT_EDITABLE
            );
        }
    }

    /* =========================================================
   SYSTEM / VK = Vereinskonto
   ========================================================= */

    @Transactional
    public FinanzGruppe getOrCreateVereinsFinanzGruppe(
            Veranstaltung veranstaltung
    ) {

        return repository
                .findByVeranstaltungIdAndKuerzel(
                        veranstaltung.getId(),
                        "VK"
                )
                .orElseGet(() -> {

                    FinanzGruppe gruppe = FinanzGruppe.builder()
                            .kuerzel("VK")
                            .system(true)
                            .veranstaltung(veranstaltung)
                            .build();

                    return repository.save(gruppe);
                });
    }

    /* =========================================================
       CREATE
       ========================================================= */

    public FinanzGruppe create(Long veranstaltungId, String kuerzel) {

        Veranstaltung veranstaltung = getVeranstaltung(veranstaltungId);

        if (kuerzel == null || kuerzel.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    KUERZEL_REQUIRED);
        }

        if (repository.existsByVeranstaltungIdAndKuerzel(veranstaltungId, kuerzel)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    KUERZEL_ALREADY_EXISTS);
        }

        FinanzGruppe gruppe = FinanzGruppe.builder()
                .kuerzel(kuerzel)
                .veranstaltung(veranstaltung)
                .build();

        return repository.save(gruppe);
    }


    /* =========================================================
       UPDATE
       ========================================================= */

    public FinanzGruppe update(Long veranstaltungId,
                               Long gruppeId,
                               String newKuerzel) {

        FinanzGruppe gruppe = getGruppe(gruppeId);

        validateVeranstaltung(gruppe, veranstaltungId);

        checkNotSystem(gruppe);

        if (newKuerzel == null || newKuerzel.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.KUERZEL_REQUIRED);
        }

        if (!gruppe.getKuerzel().equals(newKuerzel)
                && repository.existsByVeranstaltungIdAndKuerzel(
                veranstaltungId,
                newKuerzel
        )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.KUERZEL_ALREADY_EXISTS
            );
        }

        // 🔒 NEUE ARCHITEKTUR:
        // Blockieren, wenn Belege existieren
        boolean hasBelege =
                belegRepository.existsByFinanzGruppe_Id(gruppeId);

        if (hasBelege) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                   KUERZEL_CHANGE_NOT_ALLOWED_WITH_BELEGE);
        }

        gruppe.setKuerzel(newKuerzel);

        return gruppe;
    }

    /* =========================================================
       DELETE
       ========================================================= */
    @Transactional
    public void deleteByVeranstaltungId(Long veranstaltungId) {
        finanzGruppeRepository.deleteByVeranstaltungId(veranstaltungId);
    }

    public void delete(Long veranstaltungId, Long gruppeId) {

        FinanzGruppe gruppe = repository
                .findById(gruppeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                               ErrorMessages.FINANZGRUPPE_NOT_FOUND
                        )
                );

        checkNotSystem(gruppe);

        if (!gruppe.getVeranstaltung().getId().equals(veranstaltungId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                   GRUPPE_NOT_IN_VERANSTALTUNG
            );
        }

        // 🔒 Teilnehmer prüfen
        if (teilnehmerRepository.existsByFinanzGruppe_Id(gruppeId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                   KUERZEL_CANNOT_BE_DELETED_WITH_TEILNEHMER
            );
        }

        // 🔒 Belege prüfen
        if (belegRepository.existsByFinanzGruppe_Id(gruppeId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                   ErrorMessages.KUERZEL_CANNOT_BE_DELETED_WITH_BELEGE
            );
        }


        repository.delete(gruppe);
    }

    /* =========================================================
       ASSIGN KÜRZEL AN TEILNEHMER
       ========================================================= */

    public void assignKuerzel(Long veranstaltungId,
                              Long teilnehmerId,
                              String kuerzel) {

        if (kuerzel == null || kuerzel.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                   ErrorMessages.KUERZEL_REQUIRED);
        }

        Teilnehmer teilnehmer = teilnehmerRepository.findById(teilnehmerId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                TEILNEHMER_NOT_FOUND
                        ));

        if (!teilnehmer.getVeranstaltung().getId().equals(veranstaltungId)) {
            throw new BusinessRuleViolationException(
                    TEILNEHMER_NOT_IN_VERANSTALTUNG
            );
        }

        FinanzGruppe gruppe = repository
                .findByVeranstaltungIdAndKuerzel(veranstaltungId, kuerzel)
                .orElseGet(() -> {
                    FinanzGruppe g = new FinanzGruppe();
                    g.setKuerzel(kuerzel);
                    g.setVeranstaltung(teilnehmer.getVeranstaltung());
                    return repository.save(g);
                });

        FinanzGruppe aktuelle = teilnehmer.getFinanzGruppe();

        if (aktuelle != null && !aktuelle.getId().equals(gruppe.getId())) {

            // Wechsel nur erlaubt, wenn KEINE Belege für alte Gruppe existieren
            boolean hasBelege =
                    belegRepository.existsByFinanzGruppe_Id(aktuelle.getId());

            if (hasBelege) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        ErrorMessages.KUERZEL_CANNOT_BE_DELETED_WITH_BELEGE);
            }
        }

        teilnehmer.setFinanzGruppe(gruppe);
    }
    @Transactional
    public void replaceTeilnehmer(
            Long veranstaltungId,
            Long gruppeId,
            List<Long> neueIds
    ) {

        FinanzGruppe gruppe = getGruppe(veranstaltungId, gruppeId);

        List<Teilnehmer> bisherigeTeilnehmer =
                teilnehmerRepository.findAllByFinanzGruppe_Id(gruppeId);

        for (Teilnehmer t : bisherigeTeilnehmer) {
            gruppe.removeTeilnehmer(t);
        }

        List<Teilnehmer> neueTeilnehmer =
                teilnehmerRepository.findAllById(neueIds);

        for (Teilnehmer t : neueTeilnehmer) {

            if (!t.getVeranstaltung().getId().equals(veranstaltungId)) {
                throw new BusinessRuleViolationException(
                        ErrorMessages.TEILNEHMER_NOT_IN_VERANSTALTUNG
                );
            }

            gruppe.addTeilnehmer(t);
        }
    }

    @Transactional
    public void assignTeilnehmerBulk(
            Long veranstaltungId,
            Long gruppeId,
            List<Long> teilnehmerIds   // ← WIEDER TeilnehmerIds
    ) {

        FinanzGruppe gruppe = repository
                .findById(gruppeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                ErrorMessages.FINANZGRUPPE_NOT_FOUND
                        )
                );

        if (!gruppe.getVeranstaltung().getId().equals(veranstaltungId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.GRUPPE_NOT_IN_VERANSTALTUNG
            );
        }

        for (Long teilnehmerId : teilnehmerIds) {

            Teilnehmer teilnehmer = teilnehmerRepository
                    .findByIdAndVeranstaltung_Id(teilnehmerId, veranstaltungId)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    ErrorMessages.TEILNEHMER_IN_VERANSTALTUNG_NOT_FOUND
                            )
                    );

            teilnehmer.setFinanzGruppe(gruppe);
        }
    }

    @Transactional
    public void assignTeilnehmerBulkByPersonIds(
            Long veranstaltungId,
            Long gruppeId,
            List<Long> personIds
    ) {

        List<Long> teilnehmerIds = personIds.stream()
                .map(personId ->
                        teilnehmerRepository
                                .findByVeranstaltungIdAndPersonId(veranstaltungId, personId)
                                .orElseThrow(() ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                TEILNEHMER_NOT_FOUND
                                        )
                                )
                                .getId()
                )
                .toList();

        assignTeilnehmerBulk(veranstaltungId, gruppeId, teilnehmerIds);
    }
    /* ========================================================= */

    private Veranstaltung getVeranstaltung(Long id) {
        return veranstaltungRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                VERANSTALTUNG_NOT_FOUND));
    }

    private FinanzGruppe getGruppe(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                ErrorMessages.FINANZGRUPPE_NOT_FOUND));
    }

    private void validateVeranstaltung(FinanzGruppe g, Long vid) {
        if (!g.getVeranstaltung().getId().equals(vid)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                   GRUPPE_BELONGS_TO_OTHER_VERANSTALTUNG);
        }
    }
    @Transactional
    public void addTeilnehmer(
            Long veranstaltungId,
            Long gruppeId,
            List<Long> ids
    ) {

        FinanzGruppe gruppe = getGruppe(veranstaltungId, gruppeId);

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository.findAllById(ids);

        for (Teilnehmer t : teilnehmer) {

            if (!t.getVeranstaltung().getId().equals(veranstaltungId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        TEILNEHMER_IN_VERANSTALTUNG_NOT_FOUND
                );
            }

            t.setFinanzGruppe(gruppe);
        }
    }
    @Transactional
    public void removeTeilnehmer(
            Long veranstaltungId,
            Long gruppeId,
            List<Long> ids
    ) {

        FinanzGruppe gruppe = getGruppe(veranstaltungId, gruppeId);

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository.findAllById(ids);

        for (Teilnehmer t : teilnehmer) {

            if (!gruppe.equals(t.getFinanzGruppe())) {
                continue;
            }

            t.setFinanzGruppe(null);
        }
    }

    @Transactional
    public void removeTeilnehmerFromGruppe(
            Long veranstaltungId,
            Long gruppeId,
            Long personId
    ) {

        FinanzGruppe gruppe = repository
                .findById(gruppeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                               ErrorMessages.FINANZGRUPPE_NOT_FOUND
                        )
                );

        if (!gruppe.getVeranstaltung().getId().equals(veranstaltungId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.GRUPPE_NOT_IN_VERANSTALTUNG
            );
        }

        Teilnehmer teilnehmer = teilnehmerRepository
                .findByVeranstaltungIdAndPersonId(veranstaltungId, personId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                TEILNEHMER_NOT_FOUND
                        )
                );

        // 🔥 WICHTIG
        teilnehmer.setFinanzGruppe(null);
    }

    @Transactional(readOnly = true)
    public FinanzGruppe requireGemeinsameFinanzGruppe(
            Long veranstaltungId,
            List<Long> teilnehmerIds
    ) {
        List<Teilnehmer> teilnehmer =
                teilnehmerRepository.findAllById(teilnehmerIds);

        if (teilnehmer.size() != teilnehmerIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    TEILNEHMER_IN_VERANSTALTUNG_NOT_FOUND
            );
        }

        FinanzGruppe gemeinsameGruppe = null;

        for (Teilnehmer t : teilnehmer) {

            if (!t.getVeranstaltung().getId().equals(veranstaltungId)) {
                throw new BusinessRuleViolationException(
                        TEILNEHMER_NOT_IN_VERANSTALTUNG
                );
            }

            FinanzGruppe gruppe = t.getFinanzGruppe();

            if (gruppe == null) {
                throw new BusinessRuleViolationException(
                        UEBERZAHLUNG_REQUIRES_FINANZGRUPPE
                );
            }

            if (gemeinsameGruppe == null) {
                gemeinsameGruppe = gruppe;
                continue;
            }

            if (!gemeinsameGruppe.getId().equals(gruppe.getId())) {
                throw new BusinessRuleViolationException(
                        UEBERZAHLUNG_REQUIRES_COMMON_FINANZGRUPPE
                );
            }
        }

        return gemeinsameGruppe;
    }

    private FinanzGruppe getGruppe(Long veranstaltungId, Long gruppeId) {

        FinanzGruppe gruppe = repository
                .findById(gruppeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                               ErrorMessages.FINANZGRUPPE_NOT_FOUND
                        )
                );

        if (!gruppe.getVeranstaltung().getId().equals(veranstaltungId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                   ErrorMessages.GRUPPE_NOT_IN_VERANSTALTUNG
            );
        }

        return gruppe;
    }
}