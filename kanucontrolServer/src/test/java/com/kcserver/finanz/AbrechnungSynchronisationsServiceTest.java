package com.kcserver.finanz;

import com.kcserver.entity.*;
import com.kcserver.enumtype.BuchungsHerkunft;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.repository.abrechnung.AbrechnungRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.service.FoerderService;
import com.kcserver.service.abrechnung.AbrechnungBelegService;
import com.kcserver.service.abrechnung.AbrechnungSynchronisationsService;
import com.kcserver.service.beitrag.TeilnehmerBeitragService;
import com.kcserver.service.reisekosten.ReisekostenabrechnungService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class AbrechnungSynchronisationsServiceTest {

    @Mock
    private AbrechnungBelegService belegService;

    @Mock
    private TeilnehmerRepository teilnehmerRepository;

    @Mock
    private TeilnehmerBeitragService beitragService;

    @Mock
    private AbrechnungRepository abrechnungRepository;

    @Mock
    private ReisekostenabrechnungService reisekostenService;

    @Mock
    private FoerderService foerderService;

    @InjectMocks
    private AbrechnungSynchronisationsService service;

    @Test
    void shouldCreateBookingsForPaidParticipants() {

        Veranstaltung veranstaltung = new Veranstaltung();
        veranstaltung.setId(1L);

        Abrechnung abrechnung = new Abrechnung();
        abrechnung.setVeranstaltung(veranstaltung);

        AbrechnungBeleg beleg = new AbrechnungBeleg();

        Person person = new Person();
        person.setVorname("Max");
        person.setName("Mustermann");

        Teilnehmer t1 = new Teilnehmer();
        t1.setPerson(person);
        t1.setBezahlt(true);

        Teilnehmer t2 = new Teilnehmer();
        t2.setPerson(person);
        t2.setBezahlt(true);

        when(abrechnungRepository.findByVeranstaltungId(1L))
                .thenReturn(Optional.of(abrechnung));

        when(belegService.getOrCreateSystemBeleg(
                any(),
                eq(BuchungsHerkunft.TEILNEHMERBEITRAG)))
                .thenReturn(beleg);

        when(teilnehmerRepository.findAllWithPerson(1L))
                .thenReturn(List.of(t1, t2));

        when(beitragService.isBezahlt(any()))
                .thenReturn(true);

        when(beitragService.getSollBeitrag(
                any(),
                any()))
                .thenReturn(new BigDecimal("50.00"));

        when(belegService.getOrCreateSystemBeleg(
                any(),
                eq(BuchungsHerkunft.KJFP)))
                .thenReturn(new AbrechnungBeleg());

        when(belegService.getOrCreateSystemBeleg(
                any(),
                eq(BuchungsHerkunft.FAHRTKOSTEN)))
                .thenReturn(new AbrechnungBeleg());

        when(reisekostenService.findByVeranstaltung(anyLong()))
                .thenReturn(List.of());

        when(foerderService.berechneKjfpZuschuss(any(), any()))
                .thenReturn(BigDecimal.ZERO);

        service.synchronisieren(1L);

        assertThat(beleg.getPositionen())
                .hasSize(2);

        assertThat(beleg.getPositionen())
                .extracting(AbrechnungBuchung::getKategorie)
                .containsOnly(FinanzKategorie.TEILNEHMERBEITRAG);

        assertThat(beleg.getPositionen())
                .extracting(AbrechnungBuchung::getHerkunft)
                .containsOnly(BuchungsHerkunft.TEILNEHMERBEITRAG);

        BigDecimal summe = beleg.getPositionen()
                .stream()
                .map(AbrechnungBuchung::getBetrag)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(summe)
                .isEqualByComparingTo("100.00");
    }
}
