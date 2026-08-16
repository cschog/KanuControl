package com.kcserver.service.finanz;

import com.kcserver.dto.finanzen.FinanzGruppeDetailDTO;
import com.kcserver.dto.finanzen.FinanzGruppeOverviewDTO;
import com.kcserver.entity.FinanzGruppe;
import com.kcserver.mapper.FinanzGruppeDetailMapper;
import com.kcserver.mapper.FinanzGruppeOverviewMapper;
import com.kcserver.repository.abrechnung.AbrechnungBelegRepository;
import com.kcserver.repository.FinanzGruppeRepository;
import com.kcserver.repository.abrechnung.AbrechnungBuchungRepository;
import com.kcserver.repository.abrechnung.ZahlungsnachweisRepository;
import com.kcserver.repository.fahrkosten.ReisekostenabrechnungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class FinanzGruppeQueryService {

    private final FinanzGruppeRepository repository;
    private final AbrechnungBelegRepository belegRepository;
    private final FinanzGruppeOverviewMapper overviewMapper;
    private final FinanzGruppeDetailMapper detailMapper;
    private final AbrechnungBuchungRepository buchungRepository;
    private final ZahlungsnachweisRepository zahlungsnachweisRepository;
    private final ReisekostenabrechnungRepository reisekostenRepository;


    @Transactional(readOnly = true)
    public List<FinanzGruppeOverviewDTO> getOverview(Long veranstaltungId) {

        List<FinanzGruppe> gruppen =
                repository.findWithTeilnehmerByVeranstaltungId(veranstaltungId);

        // =========================================================
        // 1. Beleg-Counts
        // =========================================================

        Map<Long, Long> belegCountMap =
                belegRepository.countByVeranstaltungGrouped(veranstaltungId)
                        .stream()
                        .collect(Collectors.toMap(
                                row -> (Long) row[0],
                                row -> (Long) row[1]
                        ));

        // =========================================================
        // 2. Einnahmen / Ausgaben aus AbrechnungBuchung
        // =========================================================

        Map<Long, FinanzSummen> buchungsSummenMap =
                buchungRepository
                        .sumFinanzenByVeranstaltungGrouped(veranstaltungId)
                        .stream()
                        .collect(Collectors.toMap(
                                row -> (Long) row[0],
                                row -> new FinanzSummen(
                                        (BigDecimal) row[1],
                                        (BigDecimal) row[2]
                                )
                        ));

        // =========================================================
        // 3. Zahlungsnachweise = Einnahmen
        // =========================================================

        Map<Long, BigDecimal> zahlungsSummenMap =
                zahlungsnachweisRepository
                        .sumBetragByFinanzGruppeGrouped(veranstaltungId)
                        .stream()
                        .collect(Collectors.toMap(
                                row -> (Long) row[0],
                                row -> (BigDecimal) row[1]
                        ));

        Map<Long, BigDecimal> zahlungsPositionsSummenMap =
                zahlungsnachweisRepository
                        .sumPositionBetragByFinanzGruppeGrouped(veranstaltungId)
                        .stream()
                        .collect(Collectors.toMap(
                                row -> (Long) row[0],
                                row -> (BigDecimal) row[1]
                        ));

        // =========================================================
        // 4. Reisekosten = Ausgaben
        // =========================================================

        Map<Long, BigDecimal> reisekostenMap =
                reisekostenRepository
                        .sumGesamtBetragByFinanzGruppeGrouped(
                                veranstaltungId
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                row -> (Long) row[0],
                                row -> (BigDecimal) row[1]
                        ));

        // =========================================================
        // 5. Kontenübersicht
        // =========================================================

        return gruppen.stream()
                .map(g -> {

                    long belegCount =
                            belegCountMap.getOrDefault(
                                    g.getId(),
                                    0L
                            );

                    FinanzSummen buchungen =
                            buchungsSummenMap.getOrDefault(
                                    g.getId(),
                                    new FinanzSummen(
                                            BigDecimal.ZERO,
                                            BigDecimal.ZERO
                                    )
                            );

                    BigDecimal zahlungen =
                            zahlungsSummenMap.getOrDefault(
                                    g.getId(),
                                    BigDecimal.ZERO
                            );

                    BigDecimal zahlungsPositionen =
                            zahlungsPositionsSummenMap.getOrDefault(
                                    g.getId(),
                                    BigDecimal.ZERO
                            );

                    BigDecimal reisekosten =
                            reisekostenMap.getOrDefault(
                                    g.getId(),
                                    BigDecimal.ZERO
                            );

                    BigDecimal einnahmen =
                            buchungen.einnahmen()
                                    .add(zahlungen);

                    BigDecimal ausgaben =
                            buchungen.ausgaben()
                                    .add(reisekosten);

                    return overviewMapper.toDTO(
                            g,
                            belegCount,
                            einnahmen,
                            ausgaben
                    );
                })
                .toList();
    }

    private record FinanzSummen(
            BigDecimal einnahmen,
            BigDecimal ausgaben
    ) {}
    @Transactional(readOnly = true)
    public FinanzGruppeDetailDTO getDetail(Long gruppeId) {

        FinanzGruppe gruppe = repository.findWithTeilnehmer(gruppeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Finanzgruppe nicht gefunden"));

        return detailMapper.toDetailDTO(gruppe);
    }
}
