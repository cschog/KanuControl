package com.kcserver.finanz;

import com.kcserver.dto.abrechnung.AbrechnungBelegDTO;
import com.kcserver.dto.abrechnung.AbrechnungBuchungDTO;
import com.kcserver.dto.abrechnung.AbrechnungDetailDTO;
import com.kcserver.dto.finanzen.FinanzSummaryDTO;
import com.kcserver.dto.validation.ValidationResultDTO;
import com.kcserver.entity.*;
import com.kcserver.enumtype.AbrechnungsStatus;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.exception.ErrorMessages;
import com.kcserver.mapper.AbrechnungMapper;
import com.kcserver.repository.*;
import com.kcserver.service.FoerdersatzService;
import com.kcserver.service.beitrag.TeilnehmerBeitragService;
import com.kcserver.service.veranstaltung.VeranstaltungValidator;
import com.kcserver.service.reisekosten.ReisekostenabrechnungService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class AbrechnungService {

    private final AbrechnungRepository abrechnungRepository;
    private final AbrechnungBelegRepository abrechnungBelegRepository;
    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final FinanzGruppeRepository finanzGruppeRepository;
    private final AbrechnungMapper mapper;
    private final FinanzService finanzService;
    private final FoerdersatzService foerdersatzService;
    private final ReisekostenabrechnungService reisekostenabrechnungService;
    private final TeilnehmerBeitragService teilnehmerBeitragService;
    private final VeranstaltungValidator validator;


    /* =========================================================
       GET OR CREATE
       ========================================================= */

    public AbrechnungDetailDTO getOrCreate(Long veranstaltungId) {

        Abrechnung a = abrechnungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseGet(() -> createAbrechnung(veranstaltungId));

        AbrechnungDetailDTO dto = mapper.toDTO(a);

        BigDecimal reisekosten =
                reisekostenabrechnungService.getReisekostenSumme(
                        veranstaltungId
                );

        if (reisekosten.compareTo(BigDecimal.ZERO) > 0) {

            AbrechnungBuchungDTO position =
                    new AbrechnungBuchungDTO();

            position.setId(-1L);
            position.setKategorie(FinanzKategorie.FAHRTKOSTEN);
            position.setBeschreibung(
                    "Reisekostenabrechnungen"
            );
            position.setBetrag(reisekosten);
            position.setSystemGenerated(true);

            AbrechnungBelegDTO beleg =
                    new AbrechnungBelegDTO();

            beleg.setId(-1L);
            beleg.setKuerzel("_SYSTEM_");
            beleg.setBeschreibung(
                    "Summe Fahrkosten"
            );

            beleg.setPositionen(
                    List.of(position)
            );

            dto.getBelege().add(beleg);
        }

        Veranstaltung veranstaltung =
                a.getVeranstaltung();

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository.findAllWithPerson(
                        veranstaltungId
                );

        BigDecimal teilnehmerbeitraege =
                teilnehmerBeitragService
                        .getBezahlteSumme(
                                veranstaltung,
                                teilnehmer
                        );

        if (teilnehmerbeitraege.compareTo(BigDecimal.ZERO) > 0) {

            AbrechnungBuchungDTO position =
                    new AbrechnungBuchungDTO();

            position.setId(-2L);
            position.setKategorie(
                    FinanzKategorie.TEILNEHMERBEITRAG
            );
            position.setBeschreibung(
                    "Bezahlte Teilnehmerbeiträge"
            );
            position.setBetrag(teilnehmerbeitraege);
            position.setSystemGenerated(true);

            AbrechnungBelegDTO beleg =
                    new AbrechnungBelegDTO();

            beleg.setId(-2L);
            beleg.setKuerzel("_SYSTEM_");
            beleg.setBeschreibung(
                    "Teilnehmerbeiträge"
            );

            beleg.setPositionen(List.of(position));

            dto.getBelege().add(beleg);
        }

        FinanzSummaryDTO summary =
                finanzService.buildSummary(
                        getAllPositionen(a),
                        teilnehmerRepository.countByVeranstaltungId(
                                veranstaltungId
                        )
                );

        reisekosten =
                reisekostenabrechnungService
                        .getReisekostenSumme(
                                veranstaltungId
                        );

        summary.setKosten(
                summary.getKosten().add(reisekosten)
        );

        summary.setEinnahmen(
                summary.getEinnahmen()
                        .add(teilnehmerbeitraege)
        );

        summary.setSaldo(
                summary.getEinnahmen()
                        .subtract(summary.getKosten())
        );

        dto.setFinanz(summary);

        return dto;
    }

    @Transactional(readOnly = true)
    public ValidationResultDTO validate(Long veranstaltungId) {

        Veranstaltung veranstaltung =
                veranstaltungRepository
                        .findByIdWithRelations(veranstaltungId)
                        .orElseThrow();

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository
                        .findAllWithPerson(veranstaltungId);

        return validator.getAbrechnungValidation(
                veranstaltung,
                teilnehmer
        );
    }

    /* =========================================================
       AUTOMATISCHE BERECHNUNG TEILNEHMERBEITRÄGE
       ========================================================= */

    public void berechneTeilnehmerEinnahmen(Long veranstaltungId) {

        Abrechnung abrechnung = getEntity(veranstaltungId);
        checkEditable(abrechnung);

        Veranstaltung veranstaltung = abrechnung.getVeranstaltung();

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository.findAllWithPerson(veranstaltungId);

        // Alte automatische Belege entfernen
        abrechnung.getBelege().removeIf(beleg ->
                "__AUTO_TEILNEHMER__".equals(beleg.getBeschreibung())
        );

        AbrechnungBeleg beleg = new AbrechnungBeleg();
        beleg.setAbrechnung(abrechnung);
        beleg.setDatum(LocalDate.now());
        beleg.setBeschreibung("Automatisch berechnete Teilnehmerbeiträge");

        FinanzGruppe systemGruppe = getOrCreateSystemGroup(veranstaltung);
        beleg.setFinanzGruppe(systemGruppe);

        // 🔥 HIER NUMMER GENERIEREN
        generateBelegnummer(beleg);

        for (Teilnehmer t : teilnehmer) {

            BigDecimal betrag =
                    finanzService.berechneGebuehrFuerTeilnehmer(veranstaltung, t);

            if (betrag == null || betrag.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            AbrechnungBuchung pos = new AbrechnungBuchung();
            pos.setBeleg(beleg);
            pos.setKategorie(FinanzKategorie.TEILNEHMERBEITRAG);
            pos.setBetrag(betrag);

            beleg.getPositionen().add(pos);
        }

        if (!beleg.getPositionen().isEmpty()) {
            abrechnung.getBelege().add(beleg);
        }
    }

    /* =========================================================
       ABSCHLIESSEN
       ========================================================= */

    public void abschliessen(Long veranstaltungId) {

        Abrechnung abrechnung = getEntity(veranstaltungId);

        if (abrechnung.getStatus() == AbrechnungsStatus.ABGESCHLOSSEN) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "Abrechnung ist bereits abgeschlossen"
            );
        }

    /* ================================
       SALDO PRÜFEN
       ================================ */

        BigDecimal saldo = abrechnung.getBelege()
                .stream()
                .flatMap(b -> b.getPositionen().stream())
                .map(p -> switch (p.getKategorie()) {

                    case TEILNEHMERBEITRAG,
                         KJFP_ZUSCHUSS,
                         PFAND -> p.getBetrag();

                    default -> p.getBetrag().negate();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (saldo.compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "Abrechnung ist nicht ausgeglichen"
            );
        }

    /* ================================
       FÖRDERSATZ SNAPSHOT
       ================================ */

       // Veranstaltung veranstaltung = abrechnung.getVeranstaltung();
        LocalDate veranstaltungsDatum =
                abrechnung.getVeranstaltung().getBeginnDatum();

        VeranstaltungTyp typ =
                abrechnung.getVeranstaltung().getTyp();

        Foerdersatz fs = null;

        try {
            fs = foerdersatzService.findEntityGueltigFuerTypAm(
                    typ,
                    veranstaltungsDatum
            );
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw ex;
            }
        }

        if (fs != null) {
            abrechnung.setVerwendeterFoerdersatzIfOpen(
                    fs.getFoerdersatz()
            );
        }

    /* ================================
       STATUS ÄNDERN
       ================================ */

        abrechnung.setStatus(AbrechnungsStatus.ABGESCHLOSSEN);
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private List<AbrechnungBuchung> getAllPositionen(Abrechnung a) {

        return a.getBelege()
                .stream()
                .flatMap(beleg -> beleg.getPositionen().stream())
                .toList();
    }

    private Abrechnung createAbrechnung(Long veranstaltungId) {

        Veranstaltung veranstaltung = veranstaltungRepository
                .findById(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        ErrorMessages.VERANSTALTUNG_NOT_FOUND
                ));

        Abrechnung abrechnung = new Abrechnung();
        abrechnung.setVeranstaltung(veranstaltung);
        abrechnung.setStatus(AbrechnungsStatus.OFFEN);

        return abrechnungRepository.save(abrechnung);
    }

    private Abrechnung getEntity(Long veranstaltungId) {

        return abrechnungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        "Abrechnung nicht gefunden"
                ));
    }

    private void checkEditable(Abrechnung abrechnung) {

        if (abrechnung.getStatus() == AbrechnungsStatus.ABGESCHLOSSEN) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "Abrechnung ist abgeschlossen und nicht mehr änderbar"
            );
        }
    }

    private FinanzGruppe getOrCreateSystemGroup(Veranstaltung veranstaltung) {

        return finanzGruppeRepository
                .findByVeranstaltungIdAndKuerzel(
                        veranstaltung.getId(),
                        "__SYSTEM__"
                )
                .orElseGet(() -> {

                    FinanzGruppe g = FinanzGruppe.builder()
                            .kuerzel("__SYSTEM__")
                            .veranstaltung(veranstaltung)
                            .build();

                    return finanzGruppeRepository.save(g);
                });
    }
    private void generateBelegnummer(AbrechnungBeleg beleg) {
        Integer max = abrechnungBelegRepository
                .findMaxLfdNrByAbrechnungId(beleg.getAbrechnung().getId());

        int next = (max == null ? 1 : max + 1);

        beleg.setLfdNr(next);
        beleg.setBelegnummer(
                beleg.getFinanzGruppe().getKuerzel()
                        + "_" + String.format("%03d", next)
        );
    }
}