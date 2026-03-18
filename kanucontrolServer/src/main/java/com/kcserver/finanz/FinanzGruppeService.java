package com.kcserver.finanz;

import com.kcserver.dto.finanz.FinanzGruppeDTO;
import com.kcserver.entity.FinanzGruppe;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.repository.AbrechnungBelegRepository;
import com.kcserver.repository.FinanzGruppeRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FinanzGruppeService {

    private final FinanzGruppeRepository repository;
    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final AbrechnungBelegRepository belegRepository;

    private static final String SYSTEM_KUERZEL = "__SYSTEM__";

    /* =========================================================
       CREATE
       ========================================================= */

    public FinanzGruppe create(Long veranstaltungId, String kuerzel) {

        Veranstaltung veranstaltung = getVeranstaltung(veranstaltungId);

        if (kuerzel == null || kuerzel.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Kürzel ist Pflicht");
        }

        if (SYSTEM_KUERZEL.equals(kuerzel)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reserviertes Kürzel");
        }

        if (repository.existsByVeranstaltungIdAndKuerzel(veranstaltungId, kuerzel)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Kürzel existiert bereits");
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

        if (newKuerzel == null || newKuerzel.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Kürzel ist Pflicht");
        }

        if (SYSTEM_KUERZEL.equals(gruppe.getKuerzel())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "System-Gruppe darf nicht geändert werden");
        }

        if (repository.existsByVeranstaltungIdAndKuerzel(
                veranstaltungId, newKuerzel)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Kürzel existiert bereits");
        }

        // 🔒 NEUE ARCHITEKTUR:
        // Blockieren, wenn Belege existieren
        boolean hasBelege =
                belegRepository.existsByFinanzGruppe_Id(gruppeId);

        if (hasBelege) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Kürzel kann nicht geändert werden, da Belege existieren");
        }

        gruppe.setKuerzel(newKuerzel);

        return gruppe;
    }

    /* =========================================================
       DELETE
       ========================================================= */

    public void delete(Long veranstaltungId, Long gruppeId) {

        FinanzGruppe gruppe = getGruppe(gruppeId);

        validateVeranstaltung(gruppe, veranstaltungId);

        if (SYSTEM_KUERZEL.equals(gruppe.getKuerzel())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "System-Gruppe darf nicht gelöscht werden");
        }

        boolean hasBelege =
                belegRepository.existsByFinanzGruppe_Id(gruppeId);

        if (hasBelege) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Gruppe enthält Belege");
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
                    "Kürzel ist Pflicht");
        }

        Teilnehmer teilnehmer = teilnehmerRepository.findById(teilnehmerId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Teilnehmer nicht gefunden"));

        if (!teilnehmer.getVeranstaltung().getId().equals(veranstaltungId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Teilnehmer gehört nicht zur Veranstaltung");
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
                        "Kürzel kann nicht mehr geändert werden, da Belege existieren");
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

        // 🔹 1. Alle aktuellen entfernen
        for (Teilnehmer t : gruppe.getTeilnehmer()) {
            t.setFinanzGruppe(null);
        }

        gruppe.getTeilnehmer().clear();

        // 🔹 2. Neue setzen
        List<Teilnehmer> neueTeilnehmer =
                teilnehmerRepository.findAllById(neueIds);

        for (Teilnehmer t : neueTeilnehmer) {

            if (!t.getVeranstaltung().getId().equals(veranstaltungId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Teilnehmer gehört nicht zur Veranstaltung"
                );
            }

            t.setFinanzGruppe(gruppe);
        }
    }

    @Transactional
    public void assignTeilnehmerBulk(
            Long veranstaltungId,
            Long gruppeId,
            List<Long> teilnehmerIds
    ) {

        FinanzGruppe gruppe = repository
                .findById(gruppeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Finanzgruppe nicht gefunden"
                        )
                );

        if (!gruppe.getVeranstaltung().getId().equals(veranstaltungId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Gruppe gehört nicht zur Veranstaltung"
            );
        }

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository.findAllById(teilnehmerIds);

        for (Teilnehmer t : teilnehmer) {

            if (!t.getVeranstaltung().getId().equals(veranstaltungId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Teilnehmer gehört nicht zur Veranstaltung"
                );
            }

            t.setFinanzGruppe(gruppe);
        }
    }
    /* ========================================================= */

    private Veranstaltung getVeranstaltung(Long id) {
        return veranstaltungRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Veranstaltung nicht gefunden"));
    }

    private FinanzGruppe getGruppe(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "FinanzGruppe nicht gefunden"));
    }

    private void validateVeranstaltung(FinanzGruppe g, Long vid) {
        if (!g.getVeranstaltung().getId().equals(vid)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Gruppe gehört zu anderer Veranstaltung");
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
                        "Teilnehmer gehört nicht zur Veranstaltung"
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

    private FinanzGruppe getGruppe(Long veranstaltungId, Long gruppeId) {

        FinanzGruppe gruppe = repository
                .findById(gruppeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Finanzgruppe nicht gefunden"
                        )
                );

        if (!gruppe.getVeranstaltung().getId().equals(veranstaltungId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Gruppe gehört nicht zur Veranstaltung"
            );
        }

        return gruppe;
    }
}