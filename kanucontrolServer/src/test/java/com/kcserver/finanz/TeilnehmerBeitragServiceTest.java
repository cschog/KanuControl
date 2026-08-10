package com.kcserver.finanz;

import com.kcserver.entity.*;
import com.kcserver.service.AltersService;
import com.kcserver.service.beitrag.BeitragsregelService;
import com.kcserver.service.beitrag.TeilnehmerBeitragService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TeilnehmerBeitragServiceTest {

    @Mock
    private BeitragsregelService beitragsregelService;

    @Mock
    private AltersService altersService;

    @InjectMocks
    private TeilnehmerBeitragService service;

    @Test
    void shouldCalculateEffectiveFee() {

        Beitragsstruktur struktur = new Beitragsstruktur();
        struktur.setRegeln(List.of(new Beitragsregel()));

        Veranstaltung veranstaltung = new Veranstaltung();
        veranstaltung.setBeginnDatum(LocalDate.of(2026, 8, 1));
        veranstaltung.setBeitragsstruktur(struktur);

        Person person = new Person();
        person.setGeburtsdatum(LocalDate.of(2012, 1, 1));

        Teilnehmer teilnehmer = new Teilnehmer();
        teilnehmer.setPerson(person);

        Beitragsregel regel = new Beitragsregel();
        regel.setBeitrag(new BigDecimal("50.00"));

        when(altersService.berechneAlterBeiBeginn(
                person.getGeburtsdatum(),
                veranstaltung.getBeginnDatum()))
                .thenReturn(14);

        when(beitragsregelService.findPassendeRegel(
                struktur,
                14,
                null))
                .thenReturn(Optional.of(regel));

        assertThat(service.getSollBeitrag(
                veranstaltung,
                teilnehmer))
                .isEqualByComparingTo("50.00");
    }

    @Test
    void shouldReturnZeroWhenNoRuleExists() {

        Beitragsstruktur struktur = new Beitragsstruktur();
        struktur.setRegeln(List.of(new Beitragsregel()));

        Veranstaltung veranstaltung = new Veranstaltung();
        veranstaltung.setBeginnDatum(LocalDate.now());
        veranstaltung.setBeitragsstruktur(struktur);

        Person person = new Person();
        person.setGeburtsdatum(LocalDate.now().minusYears(15));

        Teilnehmer teilnehmer = new Teilnehmer();
        teilnehmer.setPerson(person);

        when(altersService.berechneAlterBeiBeginn(
                any(),
                any()))
                .thenReturn(15);

        when(beitragsregelService.findPassendeRegel(
                any(),
                anyInt(),
                any()))
                .thenReturn(Optional.empty());

        assertThat(service.getSollBeitrag(
                veranstaltung,
                teilnehmer))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldDetectPaidParticipant() {

        Teilnehmer t = new Teilnehmer();

        t.setBezahlt(true);

        assertThat(service.isBezahlt(t)).isTrue();

        t.setBezahlt(false);

        assertThat(service.isBezahlt(t)).isFalse();
    }

    @Test
    void shouldReturnZeroWhenNoBeitragsstrukturExists() {

        Veranstaltung veranstaltung = new Veranstaltung();

        Teilnehmer teilnehmer = new Teilnehmer();
        teilnehmer.setPerson(new Person());

        assertThat(service.getSollBeitrag(
                veranstaltung,
                teilnehmer))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnFalseWhenTeilnehmerIsNull() {
        assertThat(service.isBezahlt(null)).isFalse();
    }

    @Test
    void shouldReturnZeroWhenTeilnehmerIsNull() {
        assertThat(service.getSollBeitrag(null, null))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }
}
