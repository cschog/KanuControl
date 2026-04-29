package com.kcserver.service;

import com.kcserver.dto.finanzen.BetragPositionDTO;
import com.kcserver.dto.finanzen.FinanzenDashboardDTO;
import com.kcserver.dto.finanzen.FoerderungDashboardDTO;
import com.kcserver.entity.*;
import com.kcserver.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanzenDashboardService {

    private final VeranstaltungRepository veranstaltungRepository;

    private final TeilnehmerRepository teilnehmerRepository;

    private final PlanungPositionRepository planungPositionRepository;

    private final FoerderService foerderService;

    private final AbrechnungRepository abrechnungRepository;

    private final AbrechnungBuchungRepository abrechnungBuchungRepository;

    public FinanzenDashboardDTO getDashboard(
            Long veranstaltungId
    ) {

        Veranstaltung veranstaltung =
                veranstaltungRepository
                        .findByIdWithVerein(veranstaltungId)
                        .orElseThrow();

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository.findAllWithPerson(
                        veranstaltungId
                );

        List<PlanungPosition> positionen =
                planungPositionRepository
                        .findByPlanung_Veranstaltung_Id(
                                veranstaltungId
                        );

        FinanzenDashboardDTO dto =
                new FinanzenDashboardDTO();

        /* =====================================================
           PLAN KOSTEN / EINNAHMEN
           ===================================================== */

        BigDecimal planKosten =
                positionen.stream()
                        .filter(p ->
                                p.getKategorie().isKosten()
                        )
                        .map(PlanungPosition::getBetrag)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal planEinnahmen =
                positionen.stream()
                        .filter(p ->
                                p.getKategorie().isEinnahme()
                        )
                        .map(PlanungPosition::getBetrag)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        dto.setPlanKosten(planKosten);

        dto.setPlanEinnahmen(planEinnahmen);

        /* =====================================================
           IST (vorerst leer)
           ===================================================== */

        Abrechnung abrechnung =
                abrechnungRepository
                        .findByVeranstaltungId(
                                veranstaltungId
                        )
                        .orElse(null);

        List<AbrechnungBuchung> buchungen =
                abrechnung == null
                        ? List.of()
                        : abrechnungBuchungRepository
                        .findByBeleg_Abrechnung_Id(
                                abrechnung.getId()
                        );

        BigDecimal istKosten =
                buchungen.stream()
                        .filter(b ->
                                b.getKategorie().isKosten()
                        )
                        .map(AbrechnungBuchung::getBetrag)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal istEinnahmen =
                buchungen.stream()
                        .filter(b ->
                                b.getKategorie().isEinnahme()
                        )
                        .map(AbrechnungBuchung::getBetrag)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        dto.setIstKosten(istKosten);
        dto.setIstEinnahmen(istEinnahmen);

        /* =====================================================
           SALDEN
           ===================================================== */

        dto.setPlanSaldo(
                planEinnahmen.subtract(planKosten)
        );

        dto.setIstSaldo(
                dto.getIstEinnahmen()
                        .subtract(dto.getIstKosten())
        );

        /* =====================================================
           FÖRDERUNG
           ===================================================== */

        FoerderungDashboardDTO f =
                new FoerderungDashboardDTO();

        long foerderfaehige =
                foerderService.countFoerderfaehigeTeilnehmer(
                        veranstaltung,
                        teilnehmer
                );

        int tage =
                foerderService.berechneFoerdertage(
                        veranstaltung
                );

        BigDecimal gesamt =
                foerderService.berechneGesamtfoerderung(
                        veranstaltung,
                        teilnehmer
                );

        BigDecimal foerdersatz =
                foerderService.berechneAngewandtenFoerdersatz(
                        veranstaltung
                );

        f.setFoerderfaehigeTeilnehmer(
                (int) foerderfaehige
        );

        f.setFoerdertage(tage);

        f.setFoerdersatz(foerdersatz);

        f.setGesamtfoerderung(gesamt);

        dto.setFoerderung(f);

        /* =====================================================
           DETAILLISTEN
           ===================================================== */

        dto.setKostenNachKategorie(
                positionen.stream()
                        .filter(p ->
                                p.getKategorie().isKosten()
                        )
                        .map(p ->
                                new BetragPositionDTO(
                                        p.getKategorie().name(),
                                        p.getBetrag()
                                )
                        )
                        .toList()
        );

        dto.setEinnahmenNachKategorie(
                positionen.stream()
                        .filter(p ->
                                p.getKategorie().isEinnahme()
                        )
                        .map(p ->
                                new BetragPositionDTO(
                                        p.getKategorie().name(),
                                        p.getBetrag()
                                )
                        )
                        .toList()
        );

        dto.setIstKostenNachKategorie(

                buchungen.stream()

                        .filter(b ->
                                b.getKategorie().isKosten()
                        )

                        .collect(
                                Collectors.groupingBy(
                                        AbrechnungBuchung::getKategorie,
                                        Collectors.reducing(
                                                BigDecimal.ZERO,
                                                AbrechnungBuchung::getBetrag,
                                                BigDecimal::add
                                        )
                                )
                        )

                        .entrySet()

                        .stream()

                        .map(e ->
                                new BetragPositionDTO(
                                        e.getKey().name(),
                                        e.getValue()
                                )
                        )

                        .toList()
        );

        dto.setIstEinnahmenNachKategorie(

                buchungen.stream()

                        .filter(b ->
                                b.getKategorie().isEinnahme()
                        )

                        .collect(
                                Collectors.groupingBy(
                                        AbrechnungBuchung::getKategorie,
                                        Collectors.reducing(
                                                BigDecimal.ZERO,
                                                AbrechnungBuchung::getBetrag,
                                                BigDecimal::add
                                        )
                                )
                        )

                        .entrySet()

                        .stream()

                        .map(e ->
                                new BetragPositionDTO(
                                        e.getKey().name(),
                                        e.getValue()
                                )
                        )

                        .toList()
        );

        return dto;
    }
}