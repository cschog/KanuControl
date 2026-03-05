package com.kcserver.finanz;

import com.kcserver.dto.abrechnung.AbrechnungDetailDTO;
import com.kcserver.dto.finanz.FinanzSummaryDTO;
import com.kcserver.entity.Abrechnung;
import com.kcserver.entity.AbrechnungBuchung;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.AbrechnungsStatus;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.mapper.AbrechnungMapper;
import com.kcserver.repository.AbrechnungRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
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
public class AbrechnungService {

    private final AbrechnungRepository abrechnungRepository;
    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final AbrechnungMapper mapper;
    private final FinanzService finanzService;

    /* =========================================================
       GET OR CREATE
       ========================================================= */

    public AbrechnungDetailDTO getOrCreate(Long veranstaltungId) {

        Abrechnung a = abrechnungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseGet(() -> createAbrechnung(veranstaltungId));

        AbrechnungDetailDTO dto = mapper.toDTO(a);

        FinanzSummaryDTO summary =
                finanzService.buildSummary(
                        a.getBuchungen(),
                        teilnehmerRepository.countByVeranstaltungId(veranstaltungId)
                );

        dto.setFinanz(summary);

        return dto;
    }

    /* =========================================================
       AUTOMATISCHE BERECHNUNG TEILNEHMERBEITRÄGE
       ========================================================= */

    public void berechneTeilnehmerEinnahmen(Long veranstaltungId) {

        Abrechnung abrechnung = getEntity(veranstaltungId);

        checkEditable(abrechnung);

        Veranstaltung veranstaltung = abrechnung.getVeranstaltung();

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository.findByVeranstaltungWithPerson(veranstaltungId);

        BigDecimal summe =
                finanzService.berechneTeilnehmerSumme(veranstaltung, teilnehmer);

        removeAutomatischeTeilnehmerBuchung(abrechnung);

        AbrechnungBuchung buchung = new AbrechnungBuchung();
        buchung.setKategorie(FinanzKategorie.TEILNEHMERBEITRAG);
        buchung.setBetrag(summe);
        buchung.setDatum(LocalDate.now());
        buchung.setBeschreibung("Automatisch berechnete Teilnehmerbeiträge");

        abrechnung.addBuchung(buchung);
    }

    /* =========================================================
       ABSCHLIESSEN
       ========================================================= */

    public void abschliessen(Long veranstaltungId) {

        Abrechnung abrechnung = getEntity(veranstaltungId);

        checkEditable(abrechnung);

        finanzService.validateAusgeglichen(abrechnung.getBuchungen());

        abrechnung.setStatus(AbrechnungsStatus.ABGESCHLOSSEN);
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private Abrechnung createAbrechnung(Long veranstaltungId) {

        Veranstaltung veranstaltung = veranstaltungRepository
                .findById(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Veranstaltung nicht gefunden"
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
                        HttpStatus.NOT_FOUND,
                        "Abrechnung nicht gefunden"
                ));
    }

    private void checkEditable(Abrechnung abrechnung) {

        if (abrechnung.getStatus() == AbrechnungsStatus.ABGESCHLOSSEN) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Abrechnung ist abgeschlossen und nicht mehr änderbar"
            );
        }
    }

    private void removeAutomatischeTeilnehmerBuchung(Abrechnung abrechnung) {

        abrechnung.getBuchungen().removeIf(b ->
                b.getKategorie() == FinanzKategorie.TEILNEHMERBEITRAG);
    }
}