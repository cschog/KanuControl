package com.kcserver.service.abrechnung;

import com.kcserver.dto.abrechnung.AbrechnungBuchungCreateDTO;
import com.kcserver.dto.abrechnung.AbrechnungBuchungDTO;
import com.kcserver.dto.abrechnung.AbrechnungBelegCreateDTO;
import com.kcserver.dto.abrechnung.AbrechnungBelegDTO;
import com.kcserver.entity.*;
import com.kcserver.enumtype.AbrechnungsStatus;
import com.kcserver.enumtype.BuchungsHerkunft;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.exception.ErrorMessages;
import com.kcserver.mapper.AbrechnungMapper;
import com.kcserver.repository.*;
import com.kcserver.repository.abrechnung.AbrechnungBelegRepository;
import com.kcserver.repository.abrechnung.AbrechnungBuchungRepository;
import com.kcserver.repository.abrechnung.AbrechnungRepository;
import com.kcserver.repository.abrechnung.ZahlungsnachweisRepository;
import com.kcserver.service.zahlungsnachweis.ZahlungsnachweisSaldoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AbrechnungBelegService {

    private final AbrechnungRepository abrechnungRepository;
    private final AbrechnungBelegRepository belegRepository;
    private final AbrechnungBuchungRepository buchungRepository;
    private final FinanzGruppeRepository finanzGruppeRepository;
    private final AbrechnungMapper mapper;
    private final ZahlungsnachweisRepository zahlungsnachweisRepository;
    private final ZahlungsnachweisSaldoService zahlungsnachweisSaldoService;

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
                    ErrorMessages.KUERZEL_REQUIRED);
        }

        FinanzGruppe gruppe = finanzGruppeRepository
                .findByVeranstaltungIdAndKuerzel(veranstaltungId, dto.getKuerzel())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                ErrorMessages.KUERZEL_NOT_FOUND));

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
        beleg.setAussteller(dto.getAussteller());
        beleg.setExterneBelegnummer(dto.getExterneBelegnummer());

        beleg.setFinanzGruppe(gruppe);
        beleg.setBelegnummer(belegnummer);

        abrechnung.addBeleg(beleg);

        return mapper.toDTO(belegRepository.save(beleg));
    }

    public AbrechnungBelegDTO createBelegMitBuchung(
            Long veranstaltungId,
            AbrechnungBelegCreateDTO belegDto,
            AbrechnungBuchungCreateDTO buchungDto
    ) {

        // 1. Beleg erstellen (deine bestehende Logik!)
        AbrechnungBelegDTO belegDTO = createBeleg(veranstaltungId, belegDto);

        // 2. Buchung hinzufügen
        addPosition(veranstaltungId, belegDTO.getId(), buchungDto);

        // 3. Beleg mit Positionen neu laden
        AbrechnungBeleg beleg = belegRepository.findById(belegDTO.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.BELEG_NOT_FOUND_AFTER_CREATION));

        return mapper.toDTO(beleg);
    }

    @Transactional
    public AbrechnungBelegDTO updateBeleg(
            Long veranstaltungId,
            Long belegId,
            AbrechnungBelegCreateDTO dto
    ) {
        AbrechnungBeleg beleg = belegRepository
                .findById(belegId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                ErrorMessages.BELEG_NOT_FOUND
                        )
                );

        validateVeranstaltung(veranstaltungId, beleg);
        checkEditable(beleg.getAbrechnung());
        checkNotSystem(beleg);

        FinanzGruppe gruppe = finanzGruppeRepository
                .findByVeranstaltungIdAndKuerzel(
                        veranstaltungId,
                        dto.getKuerzel()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                ErrorMessages.KUERZEL_NOT_FOUND
                        )
                );

        beleg.setDatum(
                dto.getDatum() != null
                        ? dto.getDatum()
                        : beleg.getDatum()
        );

        beleg.setBeschreibung(dto.getBeschreibung());
        beleg.setAussteller(dto.getAussteller());
        beleg.setExterneBelegnummer(dto.getExterneBelegnummer());

        beleg.setFinanzGruppe(gruppe);

        return mapper.toDTO(beleg);
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
        checkNotSystem(beleg);

        AbrechnungBuchung position = new AbrechnungBuchung();

        position.setKategorie(dto.getKategorie());
        position.setBetrag(dto.getBetrag());
        position.setBeschreibung(dto.getBeschreibung());
        position.setHerkunft(BuchungsHerkunft.MANUELL);

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
                                ErrorMessages.POSITION_NOT_FOUND));

        validateVeranstaltung(veranstaltungId, pos.getBeleg());
        checkEditable(pos.getBeleg().getAbrechnung());
        checkNotSystem(pos.getBeleg());

        pos.setKategorie(dto.getKategorie());
        pos.setBetrag(dto.getBetrag());
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
                                ErrorMessages.POSITION_NOT_FOUND));

        AbrechnungBeleg beleg = pos.getBeleg();

        validateVeranstaltung(veranstaltungId, beleg);
        checkEditable(beleg.getAbrechnung());
        checkNotSystem(beleg);

        // 🔹 Beziehung lösen
        beleg.removePosition(pos);
        belegRepository.save(beleg);

        // 🔥 WICHTIG: wenn letzte Position → Beleg löschen
        if (beleg.getPositionen().isEmpty()) {

            beleg.getAbrechnung().removeBeleg(beleg);
            belegRepository.delete(beleg);
        }
    }

    /* =========================================================
       BELEG DELETE
       ========================================================= */

    public void deleteBeleg(Long veranstaltungId, Long belegId) {

        AbrechnungBeleg beleg = getBeleg(belegId);

        validateVeranstaltung(veranstaltungId, beleg);
        checkEditable(beleg.getAbrechnung());
        checkNotSystem(beleg);

        beleg.getAbrechnung().removeBeleg(beleg);
        belegRepository.delete(beleg);
    }

    /* =========================================================
   TEILNEHMERBEITRAG RÜCKZAHLEN
   ========================================================= */

    @Transactional
    public AbrechnungBuchungDTO rueckzahlungTeilnehmerbeitrag(
            Long veranstaltungId,
            Long zahlungsnachweisId,
            BigDecimal betrag,
            String beschreibung
    ) {

    /* =========================================================
       VALIDIERUNG BETRAG
       ========================================================= */

        if (betrag == null
                || betrag.compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Der Rückzahlungsbetrag muss größer als 0 sein."
            );
        }

    /* =========================================================
       ZAHLUNGSNACHWEIS LADEN
       ========================================================= */

        Zahlungsnachweis zahlungsnachweis =
                zahlungsnachweisRepository
                        .findByIdAndVeranstaltungId(
                                zahlungsnachweisId,
                                veranstaltungId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Zahlungsnachweis nicht gefunden."
                                )
                        );

    /* =========================================================
       OFFENE ÜBERZAHLUNG PRÜFEN
       ========================================================= */

        BigDecimal nochZurueckzahlbar =
                zahlungsnachweisSaldoService
                        .getOffeneUeberzahlung(
                                zahlungsnachweis
                        );

        if (nochZurueckzahlbar.compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Für diesen Zahlungsnachweis besteht "
                            + "keine rückzahlbare Überzahlung."
            );
        }

        if (betrag.compareTo(nochZurueckzahlbar) > 0) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Der Rückzahlungsbetrag überschreitet "
                            + "die noch verfügbare Überzahlung."
            );
        }

    /* =========================================================
       ABRECHNUNG LADEN
       ========================================================= */

        Abrechnung abrechnung =
                getAbrechnung(veranstaltungId);

        checkEditable(abrechnung);

/* =========================================================
   FINANZGRUPPE DER ÜBERZAHLUNG ERMITTELN
   ========================================================= */

        /*
         * Die ursprüngliche Zahlung kann auf dem VK-Konto
         * eingegangen sein. Die Überzahlung gehört jedoch
         * zur gemeinsamen Finanzgruppe der betroffenen
         * Teilnehmer und wurde beim Zahlungsnachweis in
         * ueberzahlungsFinanzGruppe gespeichert.
         */
        FinanzGruppe finanzGruppe =
                zahlungsnachweis.getUeberzahlungsFinanzGruppe();

        if (finanzGruppe == null) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.UEBERZAHLUNG_REQUIRES_FINANZGRUPPE
            );
        }

    /* =========================================================
       NÄCHSTE BELEGNUMMER
       ========================================================= */

        Integer max =
                belegRepository
                        .findMaxLfdNrByAbrechnungId(
                                abrechnung.getId()
                        );

        int next = max == null
                ? 1
                : max + 1;

        String belegnummer =
                finanzGruppe.getKuerzel()
                        + "_"
                        + String.format("%03d", next);

    /* =========================================================
       RÜCKZAHLUNGSBELEG ANLEGEN
       ========================================================= */

        AbrechnungBeleg beleg =
                new AbrechnungBeleg();

        beleg.setAbrechnung(abrechnung);
        beleg.setFinanzGruppe(finanzGruppe);

        beleg.setLfdNr(next);
        beleg.setBelegnummer(belegnummer);

        beleg.setDatum(LocalDate.now());

        beleg.setBeschreibung(
                beschreibung != null
                        && !beschreibung.isBlank()
                        ? beschreibung
                        : "Rückzahlung Teilnehmerbeitrag"
        );

        /*
         * Beziehung sauber über die Aggregate-Methode setzen,
         * falls diese vorhanden ist.
         */
        abrechnung.addBeleg(beleg);

    /* =========================================================
       RÜCKZAHLUNGSBUCHUNG ANLEGEN
       ========================================================= */

        AbrechnungBuchung buchung =
                new AbrechnungBuchung();

        buchung.setKategorie(
                FinanzKategorie.TEILNEHMERBEITRAG
        );

        /*
         * Rückzahlung reduziert den Teilnehmerbeitrag.
         */
        buchung.setBetrag(
                betrag.negate()
        );

        buchung.setBeschreibung(
                beleg.getBeschreibung()
        );

        buchung.setHerkunft(
                BuchungsHerkunft.MANUELL
        );

        /*
         * Ganz wichtig für die Nachverfolgung und
         * die Berechnung der noch offenen Überzahlung.
         */
        buchung.setUrspruenglicherZahlungsnachweis(
                zahlungsnachweis
        );

        beleg.addPosition(buchung);

    /* =========================================================
       SPEICHERN
       ========================================================= */

        belegRepository.save(beleg);

        return mapper.toDTO(buchung);
    }


    public void changeKuerzel(
            Long veranstaltungId,
            Long belegId,
            String newKuerzel) {

        if (newKuerzel == null || newKuerzel.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.KUERZEL_REQUIRED);
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
                                ErrorMessages.KUERZEL_NOT_FOUND));

        // 🔥 Wechsel nur durchführen, wenn es wirklich ein Wechsel ist
        if (beleg.getFinanzGruppe().getId()
                .equals(neueGruppe.getId())) {
            return;
        }

        beleg.setFinanzGruppe(neueGruppe);
    }

    @Transactional(readOnly = true)
    public List<AbrechnungBelegDTO> findByFinanzGruppe(
            Long veranstaltungId,
            Long finanzGruppeId
    ) {
        return belegRepository
                .findByAbrechnung_Veranstaltung_IdAndFinanzGruppe_IdOrderByDatumAscLfdNrAsc(
                        veranstaltungId,
                        finanzGruppeId
                )
                .stream()
                .map(mapper::toDTO)
                .toList();
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
                                ErrorMessages.ABRECHNUNG_NOT_FOUND));
    }

    private AbrechnungBeleg getBeleg(Long belegId) {

        return belegRepository.findById(belegId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                ErrorMessages.BELEG_NOT_FOUND));
    }

    private void validateVeranstaltung(Long veranstaltungId,
                                       AbrechnungBeleg beleg) {

        if (!beleg.getAbrechnung()
                .getVeranstaltung()
                .getId()
                .equals(veranstaltungId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.BELEG_WRONG_VERANSTALTUNG);
        }
    }

    private void checkEditable(Abrechnung abrechnung) {

        if (abrechnung.getStatus() == AbrechnungsStatus.ABGESCHLOSSEN) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                   ErrorMessages.ABRECHNUNG_CLOSED);
        }
    }

    private void checkNotSystem(AbrechnungBeleg beleg) {

        boolean systemBeleg = beleg.getPositionen().stream()
                .anyMatch(position ->
                        position.getHerkunft() != BuchungsHerkunft.MANUELL
                );

        if (systemBeleg) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.SYSTEMBELEG_NOT_EDITABLE);
        }
    }

    @Transactional
    public AbrechnungBeleg getOrCreateBeleg(
            Abrechnung abrechnung,
            FinanzGruppe finanzGruppe,
            BuchungsHerkunft herkunft
    ) {
        AbrechnungBeleg beleg =
                belegRepository
                        .findByAbrechnungAndBelegnummer(
                                abrechnung,
                                herkunft.getBelegnummer()
                        )
                        .orElseGet(() -> {

                            Integer max =
                                    belegRepository
                                            .findMaxLfdNrByAbrechnungId(
                                                    abrechnung.getId()
                                            );

                            int next =
                                    max == null ? 1 : max + 1;

                            AbrechnungBeleg neu =
                                    new AbrechnungBeleg();

                            neu.setAbrechnung(abrechnung);
                            neu.setFinanzGruppe(finanzGruppe);
                            neu.setLfdNr(next);
                            neu.setBelegnummer(
                                    herkunft.getBelegnummer()
                            );
                            neu.setDatum(LocalDate.now());

                            return belegRepository.save(neu);
                        });

        // Bestehenden Systembeleg ggf. von SYS nach VK verschieben
        beleg.setFinanzGruppe(finanzGruppe);
        beleg.setBeschreibung(
                herkunft.getBeschreibung()
        );

        return beleg;
    }
}