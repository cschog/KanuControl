// service/FinanzenDashboardService.java

package com.kcserver.service;

import com.kcserver.dto.finanzen.FinanzenDashboardDTO;
import com.kcserver.dto.finanzen.FoerderungDashboardDTO;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanzenDashboardService {

    private final VeranstaltungRepository veranstaltungRepository;

    private final TeilnehmerRepository teilnehmerRepository;

    private final FoerderService foerderService;

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

        FinanzenDashboardDTO dto =
                new FinanzenDashboardDTO();

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

        f.setFoerderfaehigeTeilnehmer(
                (int) foerderfaehige
        );

        BigDecimal foerdersatz =
                foerderService.berechneAngewandtenFoerdersatz(
                        veranstaltung
                );

        f.setFoerdersatz(foerdersatz);

        f.setFoerdertage(tage);

        f.setGesamtfoerderung(gesamt);

        dto.setFoerderung(f);

        return dto;
    }
}