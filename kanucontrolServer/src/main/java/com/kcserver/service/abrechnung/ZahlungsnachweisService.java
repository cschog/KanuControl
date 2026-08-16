package com.kcserver.service.abrechnung;

import com.kcserver.dto.zahlungsnachweis.*;
import com.kcserver.entity.*;
import com.kcserver.enumtype.Zahlungsweg;
import com.kcserver.mapper.ZahlungsnachweisMapper;
import com.kcserver.repository.FinanzGruppeRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.repository.abrechnung.ZahlungsnachweisRepository;
import com.kcserver.service.beitrag.TeilnehmerBeitragService;
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

    private final ZahlungsnachweisRepository repository;
    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final ZahlungsnachweisMapper mapper;
    private final TeilnehmerBeitragService teilnehmerBeitragService;
    private final FinanzGruppeRepository finanzGruppeRepository;

    @Transactional(readOnly = true)
    public List<ZahlungsnachweisListDTO> findByVeranstaltung(
            Long veranstaltungId
    ) {
        return repository.findListByVeranstaltungId(veranstaltungId);
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
                                        "Veranstaltung nicht gefunden"
                                )
                        );

        if (dto.getBetrag() == null
                || dto.getBetrag().compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Der Zahlungsbetrag muss größer als 0 sein."
            );
        }

        if (dto.getPositionen() == null
                || dto.getPositionen().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mindestens ein Teilnehmer muss ausgewählt werden."
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

        verteileBetrag(
                nachweis,
                veranstaltung,
                teilnehmer,
                dto.getBetrag()
        );

        nachweis = repository.save(nachweis);

        return mapper.toDetailDTO(nachweis);
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
                    "Der Zahlungsbetrag muss größer als 0 sein."
            );
        }


        Veranstaltung veranstaltung =
                veranstaltungRepository
                        .findById(veranstaltungId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Veranstaltung nicht gefunden"
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

        List<Teilnehmer> teilnehmer =
                ladeTeilnehmer(
                        veranstaltungId,
                        dto.getPositionen()
                );

        verteileBetrag(
                nachweis,
                veranstaltung,
                teilnehmer,
                dto.getBetrag()
        );

        nachweis = repository.save(nachweis);

        return mapper.toDetailDTO(nachweis);
    }

    public void delete(
            Long veranstaltungId,
            Long zahlungsnachweisId
    ) {
        repository.delete(
                getEntity(
                        veranstaltungId,
                        zahlungsnachweisId
                )
        );
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
                                            "Teilnehmer nicht gefunden"
                                    )
                            );

            if (!t.getVeranstaltung().getId().equals(veranstaltungId)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Teilnehmer gehört nicht zur Veranstaltung"
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
                                    repository.sumBetragByTeilnehmerId(
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

        return repository
                .findByIdAndVeranstaltungId(
                        zahlungsnachweisId,
                        veranstaltungId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Zahlungsnachweis nicht gefunden"
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
                                "Finanzgruppe gehört nicht zur Veranstaltung"
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
                                    "Für die Veranstaltung ist keine VK eingerichtet."
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
        return repository.findZahlungenByFinanzGruppe(
                veranstaltungId,
                finanzGruppeId
        );
    }
}