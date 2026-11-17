package com.kcserver.kjfp.usecase;

import com.kcserver.core.entity.*;
import com.kcserver.core.enumtype.TeilnehmerRolle;
import com.kcserver.kjfp.entity.*;
import com.kcserver.kjfp.entity.planung.Planung;
import com.kcserver.kjfp.enumtype.VeranstaltungTyp;
import com.kcserver.kjfp.dto.simulation.*;
import com.kcserver.kjfp.service.*;
import com.kcserver.kjfp.service.veranstaltung.VeranstaltungBerechnungsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FoerderregelnUseCaseTest {
    final FoerdersatzService saetze=mock(FoerdersatzService.class);
    final KikZuschlagService kik=mock(KikZuschlagService.class);
    final FoerderService service=new FoerderService(saetze,kik,new AltersService(),new VeranstaltungBerechnungsService());
    final LocalDate start=LocalDate.of(2027,7,1);
    Veranstaltung veranstaltung(int tage) {
        var v=new Veranstaltung();v.setTyp(VeranstaltungTyp.JEM);v.setBeginnDatum(start);v.setEndeDatum(start.plusDays(tage-1));v.setVerein(new Verein());return v;
    }
    Teilnehmer teilnehmer(String geburt) {
        var p=new Person();p.setGeburtsdatum(LocalDate.parse(geburt));var t=new Teilnehmer();t.setPerson(p);return t;
    }
    void satz(String betrag) {
        var s=new Foerdersatz();s.setFoerdersatz(new BigDecimal(betrag));when(saetze.findOptionalGueltigFuerTypAm(VeranstaltungTyp.JEM,start)).thenReturn(s);
    }
    @ParameterizedTest
    @CsvSource({"2021-07-01,true","2021-07-05,true","2021-07-06,false","2006-07-01,false","2006-07-02,true","2007-07-01,true"})
    void altersgrenzenGeltenAmBeginnUndMindestalterAuchAmLetztenTag(String geburt,boolean erlaubt) {
        assertThat(service.istFoerderfaehig(veranstaltung(5),teilnehmer(geburt))).isEqualTo(erlaubt);
    }
    @ParameterizedTest @EnumSource(TeilnehmerRolle.class)
    void leitungUndMitarbeiterWerdenNichtGefoerdert(TeilnehmerRolle rolle) {
        var t=teilnehmer("2010-01-01");t.setRolle(rolle);assertThat(service.istFoerderfaehig(veranstaltung(5),t)).isFalse();
    }
    @ParameterizedTest @CsvSource({"20,480","21,504","22,504"})
    void echteFoerderungBegrenztTageUndZaehltNurFoerderfaehige(int tage,String erwartet) {
        satz("12");var leiter=teilnehmer("2010-01-01");leiter.setRolle(TeilnehmerRolle.LEITER);
        assertThat(service.berechneKjfpZuschuss(veranstaltung(tage),List.of(teilnehmer("2010-01-01"),teilnehmer("2011-01-01"),teilnehmer("2022-01-01"),leiter))).isEqualByComparingTo(erwartet);
    }
    @ParameterizedTest @CsvSource({"0,0,14","-1,0,14","1,5,12","-5,-1,12"})
    void kikGiltInklusiveBeiderStichtage(long seit,long bis,String erwartet) {
        satz("12");var zuschlag=new KikZuschlag();zuschlag.setKikZuschlag(new BigDecimal("2"));when(kik.findOptionalGueltigAm(start)).thenReturn(zuschlag);
        var v=veranstaltung(5);v.getVerein().setKikZertifiziertSeit(start.plusDays(seit));v.getVerein().setKikZertifiziertBis(start.plusDays(bis));
        assertThat(service.berechneAngewandtenFoerdersatz(v)).isEqualByComparingTo(erwartet);
    }
    @Test void deckelGiltFuerOffiziellenSatzUndPlanungAberFallbackNurAufWunsch() {
        var v=veranstaltung(22);v.getVerein().setKikZertifiziertSeit(start.minusDays(1));
        var zuschlag=new KikZuschlag();zuschlag.setKikZuschlag(new BigDecimal("9"));
        when(kik.findOptionalGueltigAm(start)).thenReturn(zuschlag);when(kik.findOptionalOderLetztenGueltigen(start)).thenReturn(zuschlag);
        satz("12");assertThat(service.berechneAngewandtenFoerdersatz(v)).isEqualByComparingTo("15");
        when(saetze.findOptionalGueltigFuerTypAm(VeranstaltungTyp.JEM,start)).thenReturn(null);
        assertThatThrownBy(()->service.berechneAngewandtenFoerdersatz(v)).isInstanceOf(IllegalStateException.class);
        assertThat(service.berechneAngewandtenFoerdersatz(v,true)).isEqualByComparingTo("15");
        var plan=new Planung();plan.setVeranstaltung(v);plan.setTeilnehmer(2);plan.setKikZertifiziert(true);
        assertThat(service.berechneGeplanteFoerderung(plan)).isEqualByComparingTo("630");
        var sim=PlanungsSimulation.builder().veranstaltung(VeranstaltungsInfo.builder().beginnDatum(start).typ(VeranstaltungTyp.JEM).tage(22).vereinKikZertifiziert(true).build()).teilnehmer(2).build();
        assertThat(service.berechneGeplanteFoerderung(sim)).isEqualByComparingTo("630");
        sim.getVeranstaltung().setVereinKikZertifiziert(false);
        assertThat(service.berechneGeplanteFoerderung(sim)).isEqualByComparingTo("420");
    }
    @Test void unvollstaendigeDatenUndSportSindNichtFoerderfaehig() {
        var v=veranstaltung(5);var t=new Teilnehmer();assertThat(service.istFoerderfaehig(v,t)).isFalse();t.setPerson(new Person());assertThat(service.istFoerderfaehig(v,t)).isFalse();
        v.setTyp(VeranstaltungTyp.SPORT);assertThat(service.berechneKjfpZuschuss(v,List.of(teilnehmer("2010-01-01")))).isZero();
        verifyNoInteractions(saetze,kik);
    }
}
