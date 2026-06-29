package com.kcserver.unit;

import com.kcserver.entity.Beitragsregel;
import com.kcserver.entity.Beitragsstruktur;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.service.BeitragsregelService;
import com.kcserver.service.FoerderService;
import com.kcserver.service.PlanungBerechnungService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;


import java.util.Optional;

import static org.mockito.Mockito.when;


import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class PlanungBerechnungServiceTest {

    @Mock
    private BeitragsregelService beitragsregelService;

    @Mock
    private FoerderService foerderService;

    @InjectMocks
    private PlanungBerechnungService service;

    @Test
    void berechneTeilnehmerbeitraege_standardgebuehr() {

        Veranstaltung veranstaltung = Veranstaltung.builder()
                .individuelleGebuehren(false)
                .standardGebuehr(BigDecimal.valueOf(80))
                .geplanteTeilnehmerMaennlich(10)
                .geplanteTeilnehmerWeiblich(12)
                .geplanteTeilnehmerDivers(2)
                .geplanteMitarbeiterMaennlich(2)
                .geplanteMitarbeiterWeiblich(3)
                .geplanteMitarbeiterDivers(1)
                .build();

        BigDecimal betrag =
                service.berechneTeilnehmerbeitraege(veranstaltung);

        assertThat(betrag)
                .isEqualByComparingTo("2400.00");
    }

    @Test
    void berechneTeilnehmerbeitraege_keineStandardgebuehr() {

        Veranstaltung veranstaltung = Veranstaltung.builder()
                .individuelleGebuehren(false)
                .standardGebuehr(null)
                .geplanteTeilnehmerMaennlich(10)
                .geplanteTeilnehmerWeiblich(12)
                .geplanteTeilnehmerDivers(2)
                .geplanteMitarbeiterMaennlich(2)
                .geplanteMitarbeiterWeiblich(3)
                .geplanteMitarbeiterDivers(1)
                .build();

        BigDecimal betrag =
                service.berechneTeilnehmerbeitraege(veranstaltung);

        assertThat(betrag)
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void berechneTeilnehmerbeitraege_beitragsstruktur() {

        Beitragsstruktur struktur = new Beitragsstruktur();

        Beitragsregel teilnehmerRegel = new Beitragsregel();
        teilnehmerRegel.setBeitrag(BigDecimal.valueOf(100));

        Beitragsregel mitarbeiterRegel = new Beitragsregel();
        mitarbeiterRegel.setBeitrag(BigDecimal.valueOf(300));

        Veranstaltung veranstaltung = Veranstaltung.builder()
                .individuelleGebuehren(true)
                .beitragsstruktur(struktur)
                .geplanteTeilnehmerMaennlich(10)
                .geplanteTeilnehmerWeiblich(8)
                .geplanteTeilnehmerDivers(0)
                .geplanteMitarbeiterMaennlich(2)
                .geplanteMitarbeiterWeiblich(2)
                .geplanteMitarbeiterDivers(1)
                .build();

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

        BigDecimal betrag =
                service.berechneTeilnehmerbeitraege(veranstaltung);

        assertThat(betrag)
                .isEqualByComparingTo("3300.00");
    }

    @Test
    void berechneTeilnehmerbeitraege_keineBeitragsstruktur() {

        Veranstaltung veranstaltung = Veranstaltung.builder()
                .individuelleGebuehren(true)
                .beitragsstruktur(null)
                .geplanteTeilnehmerMaennlich(10)
                .geplanteTeilnehmerWeiblich(8)
                .geplanteTeilnehmerDivers(0)
                .geplanteMitarbeiterMaennlich(2)
                .geplanteMitarbeiterWeiblich(2)
                .geplanteMitarbeiterDivers(1)
                .build();

        BigDecimal betrag =
                service.berechneTeilnehmerbeitraege(veranstaltung);

        assertThat(betrag)
                .isEqualByComparingTo(BigDecimal.ZERO);

        verifyNoInteractions(beitragsregelService);
    }

    @Test
    void berechneTeilnehmerbeitraege_mitarbeiterregelHatVorrang() {

        Beitragsstruktur struktur = new Beitragsstruktur();

        Beitragsregel teilnehmerRegel = new Beitragsregel();
        teilnehmerRegel.setBeitrag(BigDecimal.valueOf(100));

        Beitragsregel mitarbeiterRegel = new Beitragsregel();
        mitarbeiterRegel.setBeitrag(BigDecimal.valueOf(80));

        Veranstaltung veranstaltung = Veranstaltung.builder()
                .individuelleGebuehren(true)
                .beitragsstruktur(struktur)
                .geplanteTeilnehmerMaennlich(10)
                .geplanteTeilnehmerWeiblich(8)
                .geplanteTeilnehmerDivers(0)
                .geplanteMitarbeiterMaennlich(2)
                .geplanteMitarbeiterWeiblich(2)
                .geplanteMitarbeiterDivers(1)
                .build();

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

        BigDecimal betrag =
                service.berechneTeilnehmerbeitraege(veranstaltung);

        assertThat(betrag)
                .isEqualByComparingTo("2200.00");

        verify(beitragsregelService)
                .findPlanungsRegelTeilnehmer(struktur, 20);

        verify(beitragsregelService)
                .findPlanungsRegelMitarbeiter(struktur);
    }

    @Test
    void berechneKjfpZuschuss() {

        Veranstaltung veranstaltung = Veranstaltung.builder()
                .build();

        when(
                foerderService.berechneGeplanteFoerderung(
                        veranstaltung
                )
        ).thenReturn(BigDecimal.valueOf(1800));

        BigDecimal betrag =
                service.berechneKjfpZuschuss(
                        veranstaltung
                );

        assertThat(betrag)
                .isEqualByComparingTo("1800.00");

        verify(foerderService)
                .berechneGeplanteFoerderung(
                        veranstaltung
                );
    }
}
