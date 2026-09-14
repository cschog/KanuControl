package com.kcserver.service.finanz;

import com.kcserver.dto.finanzen.FinanzausgleichDTO;
import com.kcserver.dto.finanzen.FinanzausgleichZahlungDTO;
import com.kcserver.entity.FinanzGruppe;
import com.kcserver.entity.FinanzausgleichZahlung;
import com.kcserver.mapper.FinanzausgleichZahlungMapper;
import com.kcserver.repository.finanz.FinanzGruppeRepository;
import com.kcserver.repository.finanz.FinanzausgleichZahlungRepository;
import com.kcserver.exception.ErrorMessages;
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
public class FinanzausgleichZahlungService {

    private final FinanzausgleichZahlungRepository repository;
    private final FinanzGruppeRepository finanzGruppeRepository;
    private final FinanzausgleichZahlungMapper mapper;
    private final FinanzausgleichService finanzausgleichService;


    /* =========================================================
       ZAHLUNGEN EINER FINANZGRUPPE
       ========================================================= */

    @Transactional(readOnly = true)
    public List<FinanzausgleichZahlungDTO> findByFinanzGruppe(
            Long veranstaltungId,
            Long finanzGruppeId
    ) {

        FinanzGruppe finanzGruppe =
                getFinanzGruppe(
                        veranstaltungId,
                        finanzGruppeId
                );

        return repository
                .findByFinanzGruppeIdOrderByDatumDesc(
                        finanzGruppe.getId()
                )
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public FinanzausgleichZahlungDTO get(
            Long veranstaltungId,
            Long finanzGruppeId,
            Long zahlungId
    ) {
        return mapper.toDTO(
                getEntity(
                        veranstaltungId,
                        finanzGruppeId,
                        zahlungId
                )
        );
    }

    private FinanzausgleichZahlung getEntity(
            Long veranstaltungId,
            Long finanzGruppeId,
            Long zahlungId
    ) {
        return repository
                .findByIdAndFinanzGruppeIdAndVeranstaltungId(
                        zahlungId,
                        finanzGruppeId,
                        veranstaltungId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                ErrorMessages.FINANZAUSGLEICH_ZAHLUNG_NOT_FOUND
                        )
                );
    }


    /* =========================================================
       EINZELNE ZAHLUNG
       ========================================================= */

    @Transactional(readOnly = true)
    public FinanzausgleichZahlungDTO get(
            Long veranstaltungId,
            Long zahlungId
    ) {

        return mapper.toDTO(
                getEntity(
                        veranstaltungId,
                        zahlungId
                )
        );
    }


    /* =========================================================
       NOCH OFFENER BETRAG
       ========================================================= */

    @Transactional(readOnly = true)
    public BigDecimal getNochOffenerBetrag(
            Long veranstaltungId,
            Long finanzGruppeId
    ) {

        FinanzGruppe finanzGruppe =
                getFinanzGruppe(
                        veranstaltungId,
                        finanzGruppeId
                );

        pruefeKeineVKGruppe(finanzGruppe);

        FinanzausgleichDTO finanzausgleich =
                finanzausgleichService.getFinanzausgleich(
                        veranstaltungId,
                        finanzGruppeId
                );

        BigDecimal erstattung =
                safe(
                        finanzausgleich.getErstattungVomVK()
                );

        BigDecimal bereitsGezahlt =
                safe(
                        repository.sumBetragByFinanzGruppeId(
                                finanzGruppeId
                        )
                );

        return erstattung
                .subtract(bereitsGezahlt)
                .max(BigDecimal.ZERO);
    }


    /* =========================================================
       ZAHLUNG ANLEGEN
       ========================================================= */

    public FinanzausgleichZahlungDTO create(
            Long veranstaltungId,
            Long finanzGruppeId,
            BigDecimal betrag,
            LocalDate datum,
            String bemerkung
    ) {

        FinanzGruppe finanzGruppe =
                getFinanzGruppe(
                        veranstaltungId,
                        finanzGruppeId
                );

        pruefeKeineVKGruppe(finanzGruppe);

        if (betrag == null
                || betrag.compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.FINANZAUSGLEICH_ZAHLUNG_BETRAG_MUST_BE_POSITIVE
            );
        }

        BigDecimal nochOffen =
                getNochOffenerBetrag(
                        veranstaltungId,
                        finanzGruppeId
                );

        if (nochOffen.compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.FINANZAUSGLEICH_BEREITS_VOLLSTAENDIG_GEZAHLT
            );
        }

        if (betrag.compareTo(nochOffen) > 0) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.FINANZAUSGLEICH_ZAHLUNG_EXCEEDS_OPEN_AMOUNT
            );
        }

        FinanzausgleichZahlung zahlung =
                new FinanzausgleichZahlung();

        zahlung.setFinanzGruppe(finanzGruppe);

        zahlung.setBetrag(betrag);

        zahlung.setDatum(
                datum != null
                        ? datum
                        : LocalDate.now()
        );

        zahlung.setBemerkung(
                bemerkung != null && !bemerkung.isBlank()
                        ? bemerkung
                        : null
        );

        zahlung =
                repository.save(zahlung);

        return mapper.toDTO(zahlung);
    }


    /* =========================================================
       ZAHLUNG LÖSCHEN
       ========================================================= */

    public void delete(
            Long veranstaltungId,
            Long zahlungId
    ) {

        FinanzausgleichZahlung zahlung =
                getEntity(
                        veranstaltungId,
                        zahlungId
                );

        repository.delete(zahlung);
    }


    /* =========================================================
       INTERN
       ========================================================= */

    private FinanzGruppe getFinanzGruppe(
            Long veranstaltungId,
            Long finanzGruppeId
    ) {

        return finanzGruppeRepository
                .findById(finanzGruppeId)
                .filter(gruppe ->
                        gruppe.getVeranstaltung()
                                .getId()
                                .equals(veranstaltungId)
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                ErrorMessages.GRUPPE_NOT_IN_VERANSTALTUNG
                        )
                );
    }


    private FinanzausgleichZahlung getEntity(
            Long veranstaltungId,
            Long zahlungId
    ) {

        return repository
                .findByIdAndVeranstaltungId(
                        zahlungId,
                        veranstaltungId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                ErrorMessages.FINANZAUSGLEICH_ZAHLUNG_NOT_FOUND
                        )
                );
    }


    private void pruefeKeineVKGruppe(
            FinanzGruppe finanzGruppe
    ) {

        if ("VK".equals(finanzGruppe.getKuerzel())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.VK_KONTO_NOT_ALLOWED
            );
        }
    }


    private BigDecimal safe(
            BigDecimal value
    ) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }
}