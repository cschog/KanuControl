package com.kcserver.finanz;

import com.kcserver.core.dto.teilnehmer.TeilnehmerListDTO;
import com.kcserver.core.entity.Person;
import com.kcserver.core.entity.Teilnehmer;
import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.core.enumtype.TeilnehmerRolle;
import com.kcserver.core.mapper.TeilnehmerMapper;
import com.kcserver.core.repository.TeilnehmerRepository;
import com.kcserver.core.repository.VeranstaltungRepository;
import com.kcserver.kjfp.entity.beitraege.Beitragsregel;
import com.kcserver.kjfp.entity.beitraege.Beitragsstruktur;
import com.kcserver.kjfp.enumtype.Zahlungsstatus;
import com.kcserver.kjfp.repository.abrechnung.TeilnehmerZahlungSumme;
import com.kcserver.kjfp.repository.zahlungsnachweis.ZahlungsnachweisRepository;
import com.kcserver.kjfp.service.AltersService;
import com.kcserver.kjfp.service.beitrag.BeitragsregelService;
import com.kcserver.kjfp.service.beitrag.TeilnehmerBeitragService;
import com.kcserver.kjfp.service.teilnehmer.KjfpTeilnehmerService;
import com.kcserver.kjfp.service.zahlungsnachweis.ZahlungsstatusService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class KjfpBeitraegeZahlungsstatusTest {
    @ParameterizedTest
    @NullSource // Teilnehmer ohne Leitungs-/Mitarbeiterrolle
    @EnumSource(TeilnehmerRolle.class)
    void recalculatesExistingEntriesAfterFeeAndPaymentChanges(TeilnehmerRolle rolle) {
        var veranstaltungen = mock(VeranstaltungRepository.class);
        var teilnehmerRepository = mock(TeilnehmerRepository.class);
        var mapper = mock(TeilnehmerMapper.class);
        var zahlungen = mock(ZahlungsnachweisRepository.class);
        var beitragService = new TeilnehmerBeitragService(
                new BeitragsregelService(null), new AltersService());
        var service = new KjfpTeilnehmerService(null, null, teilnehmerRepository,
                veranstaltungen, mapper, beitragService,
                new ZahlungsstatusService(zahlungen), zahlungen, null);

        var regel = new Beitragsregel();
        regel.setRolle(rolle);
        regel.setAlterBis(17);
        regel.setBeitrag(new BigDecimal("0.00"));
        var kostenpflichtig = new Beitragsregel();
        kostenpflichtig.setRolle(rolle);
        kostenpflichtig.setBeitrag(new BigDecimal("100.00"));
        var struktur = new Beitragsstruktur();
        struktur.setRegeln(List.of(regel, kostenpflichtig));
        var veranstaltung = new Veranstaltung();
        veranstaltung.setBeginnDatum(LocalDate.of(2026, 8, 1));
        veranstaltung.setBeitragsstruktur(struktur);
        var person = new Person();
        person.setGeburtsdatum(LocalDate.of(2010, 1, 1));
        var teilnehmer = new Teilnehmer();
        teilnehmer.setId(10L);
        teilnehmer.setPerson(person);
        teilnehmer.setRolle(rolle);

        when(veranstaltungen.findById(1L)).thenReturn(Optional.of(veranstaltung));
        when(teilnehmerRepository.findAllWithPerson(1L)).thenReturn(List.of(teilnehmer));
        when(mapper.toListDTO(teilnehmer)).thenAnswer(invocation -> new TeilnehmerListDTO());
        when(zahlungen.summeZahlungenByVeranstaltung(1L)).thenReturn(List.of());
        when(zahlungen.findListByVeranstaltungId(1L)).thenReturn(List.of());
        when(zahlungen.findDetailsByVeranstaltungId(1L)).thenReturn(List.of());

        var kostenlos = service.findAllByVeranstaltungForBeitraege(1L);
        assertThat(kostenlos.getTeilnehmer().getFirst().getZahlungsstatus()).isEqualTo(Zahlungsstatus.GRUEN);
        assertThat(kostenlos.getTeilnehmer().getFirst().getGezahlterBetrag()).isZero();
        assertThat(kostenlos.getSummary().getBezahlt()).isEqualTo(1);
        assertThat(kostenlos.getSummary().getBezahltSumme()).isZero();
        assertThat(kostenlos.getSummary().getOffen()).isZero();

        // Derselbe bestehende Eintrag wird durch eine Altersänderung kostenpflichtig.
        person.setGeburtsdatum(LocalDate.of(2008, 1, 1));
        var offen = service.findAllByVeranstaltungForBeitraege(1L);
        assertThat(offen.getTeilnehmer().getFirst().getZahlungsstatus()).isEqualTo(Zahlungsstatus.ROT);
        assertThat(offen.getSummary().getOffen()).isEqualTo(1);
        assertThat(offen.getSummary().getOffenSumme()).isEqualByComparingTo("100.00");

        var zahlung = mock(TeilnehmerZahlungSumme.class);
        when(zahlung.getTeilnehmerId()).thenReturn(10L);
        when(zahlung.getGezahlterBetrag()).thenReturn(new BigDecimal("25.00"));
        when(zahlungen.summeZahlungenByVeranstaltung(1L)).thenReturn(List.of(zahlung));
        var teilweise = service.findAllByVeranstaltungForBeitraege(1L);
        assertThat(teilweise.getTeilnehmer().getFirst().getZahlungsstatus()).isEqualTo(Zahlungsstatus.GELB);
        assertThat(teilweise.getSummary().getTeilweise()).isEqualTo(1);
        assertThat(teilweise.getSummary().getBezahltSumme()).isEqualByComparingTo("25.00");

        // Eine Regeländerung wird ebenfalls beim nächsten Abruf berücksichtigt.
        kostenpflichtig.setBeitrag(new BigDecimal("25.00"));
        assertThat(service.findAllByVeranstaltungForBeitraege(1L)
                .getTeilnehmer().getFirst().getZahlungsstatus()).isEqualTo(Zahlungsstatus.GRUEN);
        kostenpflichtig.setBeitrag(new BigDecimal("50.00"));
        assertThat(service.findAllByVeranstaltungForBeitraege(1L)
                .getTeilnehmer().getFirst().getZahlungsstatus()).isEqualTo(Zahlungsstatus.GELB);

        verify(zahlungen, times(5)).summeZahlungenByVeranstaltung(1L);
        verify(zahlungen, times(5)).findListByVeranstaltungId(1L);
        verify(zahlungen, times(5)).findDetailsByVeranstaltungId(1L);
        verifyNoMoreInteractions(zahlungen); // Keine fiktiven Zahlungen oder sonstigen Schreibzugriffe.
    }
}
