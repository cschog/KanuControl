package com.kcserver.kjfp.service.abrechnung;

import com.kcserver.kjfp.dto.abrechnung.AbrechnungDetailDTO;
import com.kcserver.kjfp.dto.finanzen.FinanzSummaryDTO;
import com.kcserver.kjfp.dto.validation.ValidationResultDTO;
import com.kcserver.kjfp.entity.Foerdersatz;
import com.kcserver.kjfp.entity.Teilnehmer;
import com.kcserver.kjfp.entity.Veranstaltung;
import com.kcserver.kjfp.entity.abrechnung.Abrechnung;
import com.kcserver.kjfp.entity.abrechnung.AbrechnungBuchung;
import com.kcserver.kjfp.enumtype.AbrechnungsStatus;
import com.kcserver.kjfp.enumtype.VeranstaltungTyp;
import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.kjfp.repository.TeilnehmerRepository;
import com.kcserver.kjfp.repository.VeranstaltungRepository;
import com.kcserver.kjfp.repository.abrechnung.AbrechnungRepository;
import com.kcserver.kjfp.repository.zahlungsnachweis.ZahlungsnachweisRepository;
import com.kcserver.kjfp.service.finanz.FinanzService;
import com.kcserver.kjfp.mapper.AbrechnungMapper;
import com.kcserver.kjfp.service.FoerdersatzService;
import com.kcserver.kjfp.service.reisekosten.ReisekostenabrechnungService;
import com.kcserver.kjfp.validation.VeranstaltungValidator;
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
    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final AbrechnungMapper mapper;
    private final FinanzService finanzService;
    private final FoerdersatzService foerdersatzService;
    private final VeranstaltungValidator validator;
    private final AbrechnungSynchronisationsService synchronisationsService;
    private final ReisekostenabrechnungService reisekostenabrechnungService;
    private final ZahlungsnachweisRepository zahlungsnachweisRepository;



    /* =========================================================
       GET OR CREATE
       ========================================================= */

    public AbrechnungDetailDTO getOrCreate(Long veranstaltungId) {

        abrechnungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseGet(() -> createAbrechnung(veranstaltungId));

        synchronisationsService.synchronisieren(veranstaltungId);

        BigDecimal fahrkosten =
                reisekostenabrechnungService
                        .getReisekostenSumme(veranstaltungId);

        Abrechnung abrechnung = getEntity(veranstaltungId);

        AbrechnungDetailDTO dto = mapper.toDTO(abrechnung);

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository.findAllWithPerson(veranstaltungId);

        BigDecimal rueckzahlungen =
                zahlungsnachweisRepository
                        .sumRueckzahlungenByVeranstaltung(veranstaltungId);

        FinanzSummaryDTO summary =
                finanzService.buildSummary(
                        getAllPositionen(abrechnung),
                        teilnehmer.size(),
                        fahrkosten,
                        rueckzahlungen
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
       ABSCHLIESSEN
       ========================================================= */

    public void abschliessen(Long veranstaltungId) {

        Abrechnung abrechnung = getEntity(veranstaltungId);

        if (abrechnung.getStatus() == AbrechnungsStatus.ABGESCHLOSSEN) {
            throw new ResponseStatusException(
                    CONFLICT,
                   ErrorMessages.ABRECHNUNG_ALREADY_COMPLETED
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
                    ErrorMessages.ABRECHNUNG_NOT_BALANCED
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
                        ErrorMessages.ABRECHNUNG_NOT_FOUND
                ));
    }
}