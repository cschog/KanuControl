package com.kcserver.service.zahlungsnachweis;

import com.kcserver.dto.zahlungsnachweis.*;
import com.kcserver.entity.*;
import com.kcserver.enumtype.Zahlungsweg;
import com.kcserver.exception.ErrorMessages;
import com.kcserver.mapper.ZahlungsnachweisMapper;
import com.kcserver.repository.FinanzGruppeRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.repository.zahlungsnachweis.ZahlungsnachweisRepository;
import com.kcserver.service.beitrag.TeilnehmerBeitragService;
import com.kcserver.service.finanz.FinanzGruppeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ZahlungsnachweisService {

    private final ZahlungsnachweisRepository zahlungsnachweisRepository;
    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final ZahlungsnachweisMapper mapper;
    private final TeilnehmerBeitragService teilnehmerBeitragService;
    private final FinanzGruppeRepository finanzGruppeRepository;
    private final FinanzGruppeService finanzGruppeService;
    private final ZahlungsnachweisSaldoService zahlungsnachweisSaldoService;

    @Transactional(readOnly = true)
    public List<ZahlungsnachweisListDTO> findByVeranstaltung(
            Long veranstaltungId
    ) {
        return zahlungsnachweisRepository.findListByVeranstaltungId(veranstaltungId);
    }

    @Transactional(readOnly = true)
    public ZahlungsnachweisDetailDTO get(
            Long veranstaltungId,
            Long zahlungsnachweisId
    ) {
        return mapper.toDetailDTO(
                getEntity(veranstaltungId, zahlungsnachweisId)
        );
    }

    public ZahlungsnachweisDetailDTO create(
            Long veranstaltungId,
            ZahlungsnachweisUpdateDTO dto
    ) {

        Veranstaltung veranstaltung =
                veranstaltungRepository
                        .findById(veranstaltungId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        ErrorMessages.VERANSTALTUNG_NOT_FOUND
                                )
                        );

        if (dto.getBetrag() == null
                || dto.getBetrag().compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.ZAHLUNGSBETRAG_MUST_BE_POSITIVE
            );
        }

        if (dto.getPositionen() == null
                || dto.getPositionen().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.AT_LEAST_ONE_TEILNEHMER_REQUIRED
            );
        }

        Zahlungsnachweis nachweis =
                new Zahlungsnachweis();

        nachweis.setDatum(
                dto.getDatum() != null
                        ? dto.getDatum()
                        : LocalDate.now()
        );

        nachweis.setBetrag(dto.getBetrag());
        nachweis.setZahlungsweg(dto.getZahlungsweg());
        nachweis.setBemerkung(dto.getBemerkung());
        nachweis.setVeranstaltung(veranstaltung);
        nachweis.setFinanzGruppe(
                ermittleFinanzGruppe(
                        veranstaltungId,
                        dto.getZahlungsweg(),
                        dto.getFinanzGruppeId()
                )
        );

        List<Teilnehmer> teilnehmer =
                ladeTeilnehmer(
                        veranstaltungId,
                        dto.getPositionen()
                );

        BigDecimal gesamtOffen =
                getGesamtOffenerBeitrag(
                        veranstaltung,
                        teilnehmer,
                        null
                );

        BigDecimal ueberzahlung =
                dto.getBetrag()
                        .subtract(gesamtOffen)
                        .max(BigDecimal.ZERO);

        FinanzGruppe ueberzahlungsFinanzGruppe =
                ermittleUeberzahlungsFinanzGruppe(
                        veranstaltung,
                        teilnehmer,
                        ueberzahlung
                );

        nachweis.setUeberzahlungsFinanzGruppe(
                ueberzahlungsFinanzGruppe
        );

        verteileBetrag(
                nachweis,
                veranstaltung,
                teilnehmer,
                dto.getBetrag()
        );

        nachweis = zahlungsnachweisRepository.save(nachweis);

        return mapper.toDetailDTO(nachweis);
    }

    @Transactional(readOnly = true)
    public List<OffeneUeberzahlungDTO> findOffeneUeberzahlungen(
            Long veranstaltungId
    ) {
        return zahlungsnachweisRepository
                .findOffeneUeberzahlungen(
                        veranstaltungId
                );
    }

    @Transactional
    public ZahlungsnachweisDetailDTO update(
            Long veranstaltungId,
            Long zahlungsnachweisId,
            ZahlungsnachweisUpdateDTO dto
    ) {

        Zahlungsnachweis nachweis =
                getEntity(
                        veranstaltungId,
                        zahlungsnachweisId
                );

        if (dto.getBetrag() == null
                || dto.getBetrag().compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.ZAHLUNGSBETRAG_MUST_BE_POSITIVE
            );
        }

        /*
         * Eine Rückzahlung besitzt bewusst keine ZahlungsPositionen.
         *
         * Beim Bearbeiten dürfen deshalb keine Teilnehmerzuordnungen
         * neu berechnet oder gelöscht werden. Es werden ausschließlich
         * die Kopfdaten des Rückzahlungsnachweises geändert.
         */
        boolean istRueckzahlung =
                nachweis.getUrspruenglicherZahlungsnachweis() != null;

        if (istRueckzahlung) {

            /*
             * Bei einer Rückzahlung sind Betrag, Zahlungsweg und
             * Finanzgruppe unveränderlich.
             *
             * Änderbar sind ausschließlich Datum und Bemerkung.
             */
            nachweis.setDatum(dto.getDatum());
            nachweis.setBemerkung(dto.getBemerkung());

            nachweis =
                    zahlungsnachweisRepository.save(nachweis);

            return mapper.toDetailDTO(nachweis);
        }

        Veranstaltung veranstaltung =
                veranstaltungRepository
                        .findById(veranstaltungId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        ErrorMessages.VERANSTALTUNG_NOT_FOUND
                                )
                        );

        nachweis.setDatum(dto.getDatum());
        nachweis.setBetrag(dto.getBetrag());
        nachweis.setZahlungsweg(dto.getZahlungsweg());
        nachweis.setBemerkung(dto.getBemerkung());
        nachweis.setFinanzGruppe(
                ermittleFinanzGruppe(
                        veranstaltungId,
                        dto.getZahlungsweg(),
                        dto.getFinanzGruppeId()
                )
        );

        nachweis.clearPositionen();

        /*
         * Alte Zuordnungen wirklich entfernen,
         * bevor die neuen Zahlungspositionen berechnet werden.
         */
        zahlungsnachweisRepository.saveAndFlush(nachweis);

        List<Teilnehmer> teilnehmer =
                ladeTeilnehmer(
                        veranstaltungId,
                        dto.getPositionen()
                );

        BigDecimal gesamtOffen =
                getGesamtOffenerBeitrag(
                        veranstaltung,
                        teilnehmer,
                        zahlungsnachweisId
                );

        BigDecimal ueberzahlung =
                dto.getBetrag()
                        .subtract(gesamtOffen)
                        .max(BigDecimal.ZERO);

        FinanzGruppe ueberzahlungsFinanzGruppe =
                ermittleUeberzahlungsFinanzGruppe(
                        veranstaltung,
                        teilnehmer,
                        ueberzahlung
                );

        nachweis.setUeberzahlungsFinanzGruppe(
                ueberzahlungsFinanzGruppe
        );

        verteileBetrag(
                nachweis,
                veranstaltung,
                teilnehmer,
                dto.getBetrag()
        );

        nachweis = zahlungsnachweisRepository.save(nachweis);

        return mapper.toDetailDTO(nachweis);
    }

    public void delete(
            Long veranstaltungId,
            Long zahlungsnachweisId
    ) {
        zahlungsnachweisRepository.delete(
                getEntity(
                        veranstaltungId,
                        zahlungsnachweisId
                )
        );
    }

    @Transactional
    public ZahlungsnachweisDetailDTO rueckzahlungTeilnehmerbeitrag(
            Long veranstaltungId,
            Long zahlungsnachweisId,
            BigDecimal betrag,
            String beschreibung,
            Zahlungsweg zahlungsweg
    ) {
        if (betrag == null
                || betrag.compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.RUECKZAHLUNGSBETRAG_MUST_BE_POSITIVE
            );
        }

        if (zahlungsweg == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.RUECKZAHLUNG_ZAHLUNGSWEG_REQUIRED
            );
        }

        Zahlungsnachweis urspruenglicherZahlungsnachweis =
                getEntity(
                        veranstaltungId,
                        zahlungsnachweisId
                );

        BigDecimal nochZurueckzahlbar =
                zahlungsnachweisSaldoService
                        .getOffeneUeberzahlung(
                                urspruenglicherZahlungsnachweis
                        );

        if (nochZurueckzahlbar.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.RUECKZAHLUNG_NOT_POSSIBLE
            );
        }

        if (betrag.compareTo(nochZurueckzahlbar) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.RUECKZAHLUNG_EXCEEDS_OPEN_OVERPAYMENT
            );
        }

        FinanzGruppe finanzGruppe =
                urspruenglicherZahlungsnachweis
                        .getUeberzahlungsFinanzGruppe();

        if (finanzGruppe == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.UEBERZAHLUNG_REQUIRES_FINANZGRUPPE
            );
        }

        Zahlungsnachweis rueckzahlung =
                new Zahlungsnachweis();

        rueckzahlung.setVeranstaltung(
                urspruenglicherZahlungsnachweis.getVeranstaltung()
        );

        rueckzahlung.setDatum(LocalDate.now());

        rueckzahlung.setBetrag(betrag);

        rueckzahlung.setZahlungsweg(zahlungsweg);

        rueckzahlung.setBemerkung(
                beschreibung != null
                        && !beschreibung.isBlank()
                        ? beschreibung
                        : "Rückzahlung Teilnehmerbeitrag"
        );

        rueckzahlung.setUrspruenglicherZahlungsnachweis(
                urspruenglicherZahlungsnachweis
        );

        rueckzahlung.setFinanzGruppe(finanzGruppe);

        rueckzahlung =
                zahlungsnachweisRepository.save(rueckzahlung);

        return mapper.toDetailDTO(rueckzahlung);
    }

    private List<Teilnehmer> ladeTeilnehmer(
            Long veranstaltungId,
            List<ZahlungsPositionDTO> positionen
    ) {

        List<Teilnehmer> teilnehmer = new ArrayList<>();

        for (ZahlungsPositionDTO p : positionen) {

            Teilnehmer t =
                    teilnehmerRepository
                            .findById(p.getTeilnehmerId())
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                           ErrorMessages.TEILNEHMER_NOT_FOUND
                                    )
                            );

            if (!t.getVeranstaltung().getId().equals(veranstaltungId)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                       ErrorMessages.TEILNEHMER_NOT_IN_VERANSTALTUNG
                );
            }

            teilnehmer.add(t);
        }

        return teilnehmer;
    }

    private void verteileBetrag(
            Zahlungsnachweis nachweis,
            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer,
            BigDecimal zahlungsbetrag
    ) {

        if (zahlungsbetrag == null
                || zahlungsbetrag.compareTo(BigDecimal.ZERO) <= 0
                || teilnehmer.isEmpty()) {
            return;
        }

        Long ausgeschlossenId = nachweis.getId();

        List<TeilnehmerVerteilung> verteilungen =
                teilnehmer.stream()
                        .map(t -> {

                            BigDecimal soll =
                                    teilnehmerBeitragService.getSollBeitrag(
                                            veranstaltung,
                                            t
                                    );

                            BigDecimal bereitsBezahlt =
                                    zahlungsnachweisRepository.sumBetragByTeilnehmerId(
                                            t.getId(),
                                            ausgeschlossenId
                                    );

                            BigDecimal offen =
                                    soll.subtract(bereitsBezahlt)
                                            .max(BigDecimal.ZERO);

                            return new TeilnehmerVerteilung(
                                    t,
                                    offen
                            );
                        })
                        .filter(v ->
                                v.offen().compareTo(BigDecimal.ZERO) > 0
                        )
                        .toList();

        BigDecimal gesamtOffen =
                verteilungen.stream()
                        .map(TeilnehmerVerteilung::offen)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (gesamtOffen.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal zuVerteilen =
                zahlungsbetrag.min(gesamtOffen);

        BigDecimal verteilt = BigDecimal.ZERO;

        for (int i = 0; i < verteilungen.size(); i++) {

            TeilnehmerVerteilung v =
                    verteilungen.get(i);

            BigDecimal anteil;

            if (i == verteilungen.size() - 1) {
                anteil = zuVerteilen.subtract(verteilt);
            } else {
                anteil =
                        zuVerteilen
                                .multiply(v.offen())
                                .divide(
                                        gesamtOffen,
                                        2,
                                        RoundingMode.HALF_UP
                                );

                verteilt = verteilt.add(anteil);
            }

            if (anteil.compareTo(BigDecimal.ZERO) > 0) {

                ZahlungsPosition position =
                        new ZahlungsPosition();

                position.setTeilnehmer(v.teilnehmer());
                position.setBetrag(anteil);

                nachweis.addPosition(position);
            }
        }
    }

    private record TeilnehmerVerteilung(
            Teilnehmer teilnehmer,
            BigDecimal offen
    ) {
    }

    private Zahlungsnachweis getEntity(
            Long veranstaltungId,
            Long zahlungsnachweisId
    ) {

        return zahlungsnachweisRepository
                .findByIdAndVeranstaltungId(
                        zahlungsnachweisId,
                        veranstaltungId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                               ErrorMessages.ZAHLUNGSNACHWEIS_NOT_FOUND
                        )
                );
    }
    private FinanzGruppe ladeFinanzGruppe(
            Long veranstaltungId,
            Long finanzGruppeId
    ) {

        if (finanzGruppeId == null) {
            return null;
        }

        return finanzGruppeRepository
                .findById(finanzGruppeId)
                .filter(g ->
                        g.getVeranstaltung()
                                .getId()
                                .equals(veranstaltungId)
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                ErrorMessages.GRUPPE_NOT_IN_VERANSTALTUNG
                        )
                );
    }

    private FinanzGruppe ermittleFinanzGruppe(
            Long veranstaltungId,
            Zahlungsweg zahlungsweg,
            Long finanzGruppeId
    ) {
        if (zahlungsweg == Zahlungsweg.UEBERWEISUNG) {
            return finanzGruppeRepository
                    .findByVeranstaltungIdAndKuerzel(
                            veranstaltungId,
                            "VK"
                    )
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.CONFLICT,
                                    ErrorMessages.VK_KONTO_NOT_CONFIGURED
                            )
                    );
        }

        return ladeFinanzGruppe(
                veranstaltungId,
                finanzGruppeId
        );
    }

    @Transactional(readOnly = true)
    public List<FinanzGruppeZahlungDTO> findByFinanzGruppe(
            Long veranstaltungId,
            Long finanzGruppeId
    ) {
        return zahlungsnachweisRepository.findZahlungenByFinanzGruppe(
                veranstaltungId,
                finanzGruppeId
        );
    }

    @Transactional(readOnly = true)
    public List<FinanzGruppeZahlungDTO> findUeberweisungenByFinanzGruppe(
            Long veranstaltungId,
            Long finanzGruppeId
    ) {
        List<FinanzGruppeZahlungDTO> ueberweisungen =
                new ArrayList<>(
                        zahlungsnachweisRepository
                                .findUrspruenglicheUeberweisungenByFinanzGruppe(
                                        veranstaltungId,
                                        finanzGruppeId
                                )
                );

        ueberweisungen.addAll(
                zahlungsnachweisRepository
                        .findUeberweisungsRueckzahlungenByFinanzGruppe(
                                veranstaltungId,
                                finanzGruppeId
                        )
        );

        ueberweisungen.sort(
                java.util.Comparator
                        .comparing(
                                FinanzGruppeZahlungDTO::datum,
                                java.util.Comparator.nullsLast(
                                        java.util.Comparator.reverseOrder()
                                )
                        )
        );

        return ueberweisungen;
    }

    private BigDecimal getGesamtOffenerBeitrag(
            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer,
            Long ausgeschlossenZahlungsnachweisId
    ) {

        return teilnehmer.stream()
                .map(t -> {

                    BigDecimal soll =
                            teilnehmerBeitragService.getSollBeitrag(
                                    veranstaltung,
                                    t
                            );

                    BigDecimal bereitsBezahlt =
                            zahlungsnachweisRepository
                                    .sumBetragByTeilnehmerId(
                                            t.getId(),
                                            ausgeschlossenZahlungsnachweisId
                                    );

                    return soll
                            .subtract(bereitsBezahlt)
                            .max(BigDecimal.ZERO);

                })
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private FinanzGruppe ermittleUeberzahlungsFinanzGruppe(
            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer,
            BigDecimal ueberzahlung
    ) {
        if (ueberzahlung.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        List<Long> relevanteTeilnehmerIds =
                teilnehmer.stream()
                        .filter(t ->
                                teilnehmerBeitragService
                                        .getSollBeitrag(veranstaltung, t)
                                        .compareTo(BigDecimal.ZERO) > 0
                        )
                        .map(Teilnehmer::getId)
                        .toList();

        if (relevanteTeilnehmerIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.UEBERZAHLUNG_REQUIRES_FINANZGRUPPE
            );
        }

        return finanzGruppeService.requireGemeinsameFinanzGruppe(
                veranstaltung.getId(),
                relevanteTeilnehmerIds
        );
    }
}