package com.kcserver.finanz;

import com.kcserver.dto.abrechnung.AbrechnungBuchungCreateDTO;
import com.kcserver.dto.abrechnung.AbrechnungBuchungDTO;
import com.kcserver.dto.abrechnung.AbrechnungBelegCreateDTO;
import com.kcserver.dto.abrechnung.AbrechnungBelegDTO;
import com.kcserver.entity.*;
import com.kcserver.enumtype.AbrechnungsStatus;
import com.kcserver.mapper.AbrechnungMapper;
import com.kcserver.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class AbrechnungBelegService {

    private final AbrechnungRepository abrechnungRepository;
    private final AbrechnungBelegRepository belegRepository;
    private final AbrechnungBuchungRepository buchungRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final FinanzGruppeRepository finanzGruppeRepository;
    private final AbrechnungMapper mapper;

    /* =========================================================
       BELEG ANLEGEN
       ========================================================= */

    public AbrechnungBelegDTO createBeleg(
            Long veranstaltungId,
            AbrechnungBelegCreateDTO dto) {

        Abrechnung abrechnung = getAbrechnung(veranstaltungId);
        checkEditable(abrechnung);

        if (dto.getKuerzel() == null || dto.getKuerzel().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Kürzel ist Pflicht");
        }

        FinanzGruppe gruppe = finanzGruppeRepository
                .findByVeranstaltungIdAndKuerzel(veranstaltungId, dto.getKuerzel())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "FinanzGruppe nicht gefunden"));

        // 🔥 MAX
        Integer max = belegRepository
                .findMaxLfdNrByAbrechnungId(abrechnung.getId());

        int next = (max == null ? 1 : max + 1);

        String belegnummer =
                gruppe.getKuerzel() + "_" + String.format("%03d", next);

        AbrechnungBeleg beleg = new AbrechnungBeleg();
        beleg.setLfdNr(next);
        beleg.setDatum(dto.getDatum() != null ? dto.getDatum() : LocalDate.now());
        beleg.setBeschreibung(dto.getBeschreibung());
        beleg.setFinanzGruppe(gruppe);

        beleg.setBelegnummer(belegnummer);

        abrechnung.addBeleg(beleg);

        return mapper.toDTO(belegRepository.save(beleg));
    }

    /* =========================================================
       POSITION HINZUFÜGEN
       ========================================================= */

    public AbrechnungBuchungDTO addPosition(
            Long veranstaltungId,
            Long belegId,
            AbrechnungBuchungCreateDTO dto) {

        AbrechnungBeleg beleg = getBeleg(belegId);
        validateVeranstaltung(veranstaltungId, beleg);
        checkEditable(beleg.getAbrechnung());

        Teilnehmer teilnehmer = teilnehmerRepository.findById(dto.getTeilnehmerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Teilnehmer nicht gefunden"));

        if (!teilnehmer.getVeranstaltung().getId().equals(veranstaltungId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Teilnehmer gehört nicht zu dieser Veranstaltung");
        }

        if (teilnehmer.getFinanzGruppe() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Teilnehmer hat kein Kürzel");
        }

        // Teilnehmer muss zur gleichen Gruppe gehören wie der Beleg
        if (!teilnehmer.getFinanzGruppe().getId()
                .equals(beleg.getFinanzGruppe().getId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Teilnehmer gehört nicht zum Kürzel des Belegs");
        }

        AbrechnungBuchung position = new AbrechnungBuchung();
        position.setBeleg(beleg);
        position.setTeilnehmer(teilnehmer);
        position.setKategorie(dto.getKategorie());
        position.setBetrag(dto.getBetrag());
        position.setDatum(dto.getDatum());
        position.setBeschreibung(dto.getBeschreibung());

        beleg.addPosition(position);

        return mapper.toDTO(buchungRepository.save(position));
    }

    /* =========================================================
       POSITION UPDATE
       ========================================================= */

    public AbrechnungBuchungDTO updatePosition(
            Long veranstaltungId,
            Long positionId,
            AbrechnungBuchungCreateDTO dto) {

        AbrechnungBuchung pos = buchungRepository.findById(positionId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Position nicht gefunden"));

        validateVeranstaltung(veranstaltungId, pos.getBeleg());
        checkEditable(pos.getBeleg().getAbrechnung());

        pos.setKategorie(dto.getKategorie());
        pos.setBetrag(dto.getBetrag());
        pos.setDatum(dto.getDatum());
        pos.setBeschreibung(dto.getBeschreibung());

        return mapper.toDTO(pos);
    }

    /* =========================================================
       POSITION DELETE
       ========================================================= */

    public void deletePosition(Long veranstaltungId, Long positionId) {

        AbrechnungBuchung pos = buchungRepository.findById(positionId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Position nicht gefunden"));

        validateVeranstaltung(veranstaltungId, pos.getBeleg());
        checkEditable(pos.getBeleg().getAbrechnung());

        pos.getBeleg().removePosition(pos);
        buchungRepository.delete(pos);
    }

    /* =========================================================
       BELEG DELETE
       ========================================================= */

    public void deleteBeleg(Long veranstaltungId, Long belegId) {

        AbrechnungBeleg beleg = getBeleg(belegId);

        validateVeranstaltung(veranstaltungId, beleg);
        checkEditable(beleg.getAbrechnung());

        beleg.getAbrechnung().removeBeleg(beleg);
        belegRepository.delete(beleg);
    }

    public void changeKuerzel(
            Long veranstaltungId,
            Long belegId,
            String newKuerzel) {

        if (newKuerzel == null || newKuerzel.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Kürzel ist Pflicht");
        }

        AbrechnungBeleg beleg = getBeleg(belegId);

        validateVeranstaltung(veranstaltungId, beleg);
        checkEditable(beleg.getAbrechnung());

        FinanzGruppe neueGruppe = finanzGruppeRepository
                .findByVeranstaltungIdAndKuerzel(
                        veranstaltungId,
                        newKuerzel)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Kürzel nicht gefunden"));

        // 🔥 Wechsel nur durchführen wenn es wirklich ein Wechsel ist
        if (beleg.getFinanzGruppe().getId()
                .equals(neueGruppe.getId())) {
            return;
        }

        beleg.setFinanzGruppe(neueGruppe);
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private Abrechnung getAbrechnung(Long veranstaltungId) {

        return abrechnungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Abrechnung nicht gefunden"));
    }

    private AbrechnungBeleg getBeleg(Long belegId) {

        return belegRepository.findById(belegId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Beleg nicht gefunden"));
    }

    private void validateVeranstaltung(Long veranstaltungId,
                                       AbrechnungBeleg beleg) {

        if (!beleg.getAbrechnung()
                .getVeranstaltung()
                .getId()
                .equals(veranstaltungId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Beleg gehört zu anderer Veranstaltung");
        }
    }

    private void checkEditable(Abrechnung abrechnung) {

        if (abrechnung.getStatus() == AbrechnungsStatus.ABGESCHLOSSEN) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Abrechnung ist abgeschlossen und nicht mehr änderbar");
        }
    }
}