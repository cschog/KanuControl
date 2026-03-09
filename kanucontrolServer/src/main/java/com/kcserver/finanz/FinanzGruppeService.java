package com.kcserver.finanz;

import com.kcserver.entity.FinanzGruppe;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.repository.AbrechnungBuchungRepository;
import com.kcserver.repository.FinanzGruppeRepository;
import com.kcserver.repository.VeranstaltungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FinanzGruppeService {

    private final FinanzGruppeRepository repository;
    private final VeranstaltungRepository veranstaltungRepository;
    private final AbrechnungBuchungRepository buchungRepository;

    private static final String SYSTEM_KUERZEL = "__SYSTEM__";

    /* =========================================================
       CREATE
       ========================================================= */

    public FinanzGruppe create(Long veranstaltungId, String kuerzel) {

        Veranstaltung veranstaltung = getVeranstaltung(veranstaltungId);

        if (SYSTEM_KUERZEL.equals(kuerzel)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reserviertes Kürzel"
            );
        }

        if (repository.existsByVeranstaltungIdAndKuerzel(veranstaltungId, kuerzel)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Kürzel existiert bereits"
            );
        }

        FinanzGruppe gruppe = FinanzGruppe.builder()
                .kuerzel(kuerzel)
                .veranstaltung(veranstaltung)
                .build();

        return repository.save(gruppe);
    }

    /* =========================================================
       READ
       ========================================================= */

    @Transactional(readOnly = true)
    public List<FinanzGruppe> findAll(Long veranstaltungId) {
        return repository.findByVeranstaltungIdOrderByKuerzel(veranstaltungId);
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    public FinanzGruppe update(Long veranstaltungId,
                               Long gruppeId,
                               String newKuerzel) {

        FinanzGruppe gruppe = getGruppe(gruppeId);

        validateVeranstaltung(gruppe, veranstaltungId);

        if (SYSTEM_KUERZEL.equals(gruppe.getKuerzel())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "System-Gruppe darf nicht geändert werden"
            );
        }

        if (repository.existsByVeranstaltungIdAndKuerzel(
                veranstaltungId, newKuerzel)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Kürzel existiert bereits"
            );
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
                    "System-Gruppe darf nicht gelöscht werden"
            );
        }

        boolean hasBuchungen =
                buchungRepository.existsByFinanzGruppeId(gruppeId);

        if (hasBuchungen) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Gruppe enthält Buchungen"
            );
        }

        repository.delete(gruppe);
    }

    /* ========================================================= */

    private Veranstaltung getVeranstaltung(Long id) {
        return veranstaltungRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Veranstaltung nicht gefunden"
                        ));
    }

    private FinanzGruppe getGruppe(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "FinanzGruppe nicht gefunden"
                        ));
    }

    private void validateVeranstaltung(FinanzGruppe g, Long vid) {
        if (!g.getVeranstaltung().getId().equals(vid)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Gruppe gehört zu anderer Veranstaltung"
            );
        }
    }
}