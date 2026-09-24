package com.kcserver.service;

import com.kcserver.dto.finanzen.BetragPositionDTO;
import com.kcserver.dto.finanzen.FinanzenDashboardDTO;
import com.kcserver.dto.finanzen.FoerderungDashboardDTO;
import com.kcserver.entity.*;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.repository.*;
import com.kcserver.repository.abrechnung.AbrechnungBuchungRepository;
import com.kcserver.repository.abrechnung.AbrechnungRepository;
import com.kcserver.service.abrechnung.AbrechnungSynchronisationsService;
import com.kcserver.service.reisekosten.ReisekostenabrechnungService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FinanzenDashboardService {

    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final PlanungPositionRepository planungPositionRepository;
    private final FoerderService foerderService;
    private final AbrechnungRepository abrechnungRepository;
    private final AbrechnungBuchungRepository abrechnungBuchungRepository;
    private final PlanungRepository planungRepository;
    private final AbrechnungSynchronisationsService synchronisationsService;
    private final ReisekostenabrechnungService reisekostenabrechnungService;

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

        /*
         * Abrechnung ist optional.
         *
         * Existiert sie bereits, werden die automatischen
         * Abrechnungspositionen synchronisiert.
         */
        Abrechnung abrechnung =
                abrechnungRepository
                        .findByVeranstaltungId(
                                veranstaltungId
                        )
                        .orElse(null);

        if (abrechnung != null) {
            synchronisationsService.synchronisieren(veranstaltungId);
        }

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
           IST
           ===================================================== */

        List<AbrechnungBuchung> buchungen =
                abrechnung == null
                        ? List.of()
                        : abrechnungBuchungRepository
                        .findByBeleg_Abrechnung_Id(
                                abrechnung.getId()
                        );

        BigDecimal istKostenBuchungen =
                buchungen.stream()
                        .filter(b ->
                                b.getKategorie().isKosten()
                        )
                        .map(AbrechnungBuchung::getBetrag)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal fahrkosten =
                reisekostenabrechnungService
                        .getReisekostenSumme(veranstaltungId);

        if (fahrkosten == null) {
            fahrkosten = BigDecimal.ZERO;
        }

        BigDecimal istKosten =
                istKostenBuchungen.add(fahrkosten);

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

        Planung planung =
                planungRepository
                        .findByVeranstaltungId(veranstaltungId)
                        .orElse(null);

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
                buchungen.stream()
                        .filter(b -> b.getKategorie() == FinanzKategorie.KJFP_ZUSCHUSS)
                        .map(AbrechnungBuchung::getBetrag)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal foerdersatz =
                foerderService.berechneAngewandtenFoerdersatz(
                        planung
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
                        .filter(p -> p.getKategorie().isKosten())
                        .collect(
                                Collectors.groupingBy(
                                        PlanungPosition::getKategorie,
                                        Collectors.reducing(
                                                BigDecimal.ZERO,
                                                PlanungPosition::getBetrag,
                                                BigDecimal::add
                                        )
                                )
                        )
                        .entrySet()
                        .stream()
                        .sorted(
                                java.util.Map.Entry.comparingByKey()
                        )
                        .map(e ->
                                new BetragPositionDTO(
                                        e.getKey().name(),
                                        e.getValue()
                                )
                        )
                        .toList()
        );

        dto.setEinnahmenNachKategorie(
                positionen.stream()
                        .filter(p -> p.getKategorie().isEinnahme())
                        .collect(
                                Collectors.groupingBy(
                                        PlanungPosition::getKategorie,
                                        Collectors.reducing(
                                                BigDecimal.ZERO,
                                                PlanungPosition::getBetrag,
                                                BigDecimal::add
                                        )
                                )
                        )
                        .entrySet()
                        .stream()
                        .sorted(
                                java.util.Map.Entry.comparingByKey()
                        )
                        .map(e ->
                                new BetragPositionDTO(
                                        e.getKey().name(),
                                        e.getValue()
                                )
                        )
                        .toList()
        );

        Map<FinanzKategorie, BigDecimal> kostenNachKategorie =
                buchungen.stream()
                        .filter(b -> b.getKategorie().isKosten())
                        .collect(
                                Collectors.groupingBy(
                                        AbrechnungBuchung::getKategorie,
                                        Collectors.reducing(
                                                BigDecimal.ZERO,
                                                AbrechnungBuchung::getBetrag,
                                                BigDecimal::add
                                        )
                                )
                        );

        if (fahrkosten.compareTo(BigDecimal.ZERO) != 0) {
            kostenNachKategorie.merge(
                    FinanzKategorie.FAHRKOSTEN,
                    fahrkosten,
                    BigDecimal::add
            );
        }

        dto.setIstKostenNachKategorie(
                kostenNachKategorie
                        .entrySet()
                        .stream()
                        .sorted(
                                java.util.Map.Entry.comparingByKey()
                        )

                        .map(e ->
                                new BetragPositionDTO(
                                        e.getKey().name(),
                                        e.getValue()
                                )
                        )

                        .toList()
        );

        Map<FinanzKategorie, BigDecimal> einnahmenNachKategorie =
                buchungen.stream()
                        .filter(b -> b.getKategorie().isEinnahme())
                        .collect(
                                Collectors.groupingBy(
                                        AbrechnungBuchung::getKategorie,
                                        Collectors.reducing(
                                                BigDecimal.ZERO,
                                                AbrechnungBuchung::getBetrag,
                                                BigDecimal::add
                                        )
                                )
                        );

        dto.setIstEinnahmenNachKategorie(
                einnahmenNachKategorie
                        .entrySet()
                        .stream()
                        .sorted(
                                Map.Entry.comparingByKey()
                        )
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