package com.kcserver.unit;

import com.kcserver.dto.simulation.VeranstaltungsInfo;
import com.kcserver.entity.*;
import com.kcserver.service.beitrag.BeitragsregelService;
import com.kcserver.service.FoerderService;
import com.kcserver.service.PlanungBerechnungService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import com.kcserver.dto.simulation.PlanungsSimulation;

@ExtendWith(MockitoExtension.class)
@Disabled("Wird nach Refactoring überarbeitet")

class PlanungBerechnungServiceTest {

    @Mock
    private BeitragsregelService beitragsregelService;

    @Mock
    private FoerderService foerderService;

    @InjectMocks
    private PlanungBerechnungService service;

    @Test
    void berechneTeilnehmerbeitraegeSimulation_standardgebuehr() {

        PlanungsSimulation simulation =
                PlanungsSimulation.builder()
                        .teilnehmer(24)
                        .mitarbeiter(6)
                        .build();

        BigDecimal betrag =
                service.berechneTeilnehmerbeitraege(simulation);

        assertThat(betrag)
                .isEqualByComparingTo("2400");
    }

    @Test
    void berechneTeilnehmerbeitraegeSimulation_beitragsstruktur() {

        Beitragsstruktur struktur = new Beitragsstruktur();

        Beitragsregel teilnehmer = new Beitragsregel();
        teilnehmer.setBeitrag(BigDecimal.valueOf(100));

        Beitragsregel mitarbeiter = new Beitragsregel();
        mitarbeiter.setBeitrag(BigDecimal.valueOf(300));

        when(beitragsregelService.findPlanungsRegelTeilnehmer(
                struktur,
                20))
                .thenReturn(Optional.of(teilnehmer));

        when(beitragsregelService.findPlanungsRegelMitarbeiter(
                struktur))
                .thenReturn(Optional.of(mitarbeiter));

        PlanungsSimulation simulation =
                PlanungsSimulation.builder()
                        .teilnehmer(18)
                        .mitarbeiter(5)
         //               .beitragsstruktur(1L)
                        .build();

        BigDecimal betrag =
                service.berechneTeilnehmerbeitraege(simulation);

        assertThat(betrag)
                .isEqualByComparingTo("3300");
    }

    @Test
    void berechneTeilnehmerbeitraegeSimulation_keineBeitragsstruktur() {

        PlanungsSimulation simulation =
                PlanungsSimulation.builder()
                        .teilnehmer(18)
                        .mitarbeiter(5)
                        .build();

        assertThat(
                service.berechneTeilnehmerbeitraege(simulation)
        ).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void berechneKjfpZuschussSimulation() {

        PlanungsSimulation simulation =
                PlanungsSimulation.builder()
                        .build();

        when(
                foerderService.berechneGeplanteFoerderung(
                        simulation
                )
        ).thenReturn(BigDecimal.valueOf(1800));

        BigDecimal betrag =
                service.berechneKjfpZuschuss(
                        simulation
                );

        assertThat(betrag)
                .isEqualByComparingTo("1800.00");

        verify(foerderService)
                .berechneGeplanteFoerderung(
                        simulation
                );
    }


    @Test
    void berechneUnterkunftSimulation() {

        PlanungsSimulation simulation =
                PlanungsSimulation.builder()
                        .teilnehmer(18)
                        .mitarbeiter(6)
                        .veranstaltung(
                                VeranstaltungsInfo.builder()
                                        .naechte(5)
                                        .build()
                        )
                        .unterkunftPreisProPersonUndNacht(
                                BigDecimal.valueOf(35)
                        )
                        .build();

        BigDecimal betrag =
                service.berechneUnterkunft(simulation);

        assertThat(betrag)
                .isEqualByComparingTo("4200.00");
    }

    @Test
    void berechneVerpflegungSimulation() {

        PlanungsSimulation simulation =
                PlanungsSimulation.builder()
                        .teilnehmer(18)
                        .mitarbeiter(6)
                        .veranstaltung(
                                VeranstaltungsInfo.builder()
                                        .tage(6)
                                        .build()
                        )
                        .verpflegungPreisProPersonUndTag(
                                BigDecimal.valueOf(18)
                        )
                        .build();

        BigDecimal betrag =
                service.berechneVerpflegung(simulation);

        assertThat(betrag)
                .isEqualByComparingTo("2592.00");
    }

    @Test
    void berechneUnterkunftOhnePreis() {

        PlanungsSimulation simulation =
                PlanungsSimulation.builder()
                        .teilnehmer(20)
                        .veranstaltung(
                                VeranstaltungsInfo.builder()
                                        .naechte(5)
                                        .build()
                        )
                        .build();

        assertThat(
                service.berechneUnterkunft(simulation)
        ).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void berechneVerpflegungOhnePreis() {

        PlanungsSimulation simulation =
                PlanungsSimulation.builder()
                        .teilnehmer(20)
                        .veranstaltung(
                                VeranstaltungsInfo.builder()
                                        .tage(6)
                                        .build()
                        )
                        .build();

        assertThat(
                service.berechneVerpflegung(simulation)
        ).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void berechneTeilnehmerbeitraege_keineStandardgebuehr() {

        PlanungsSimulation simulation =
                PlanungsSimulation.builder()
                        .teilnehmer(24)
                        .mitarbeiter(6)
                        .build();

        BigDecimal betrag =
                service.berechneTeilnehmerbeitraege(simulation);

        assertThat(betrag)
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void berechneTeilnehmerbeitraege_berechnetTeilnehmerUndMitarbeiterGetrennt() {

        Beitragsstruktur struktur = new Beitragsstruktur();

        Beitragsregel teilnehmerRegel = new Beitragsregel();
        teilnehmerRegel.setBeitrag(BigDecimal.valueOf(100));

        Beitragsregel mitarbeiterRegel = new Beitragsregel();
        mitarbeiterRegel.setBeitrag(BigDecimal.valueOf(80));

        when(
                beitragsregelService.findPlanungsRegelTeilnehmer(
                        struktur,
                        20
                )
        ).thenReturn(Optional.of(teilnehmerRegel));

        when(
                beitragsregelService.findPlanungsRegelMitarbeiter(
                        struktur
                )
        ).thenReturn(Optional.of(mitarbeiterRegel));

        PlanungsSimulation simulation =
                PlanungsSimulation.builder()
                        .teilnehmer(18)
                        .mitarbeiter(5)
            //            .beitragsstruktur(struktur)
                        .build();

        BigDecimal betrag =
                service.berechneTeilnehmerbeitraege(simulation);

        assertThat(betrag)
                .isEqualByComparingTo("2200.00");
    }
}
