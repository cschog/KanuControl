package com.kcserver.kjfp.usecase;

import com.kcserver.kjfp.service.*;
import com.kcserver.kjfp.service.planung.PlanungBerechnungService;
import com.kcserver.kjfp.service.simulation.SimulationEngine;
import com.kcserver.kjfp.service.veranstaltung.VeranstaltungBerechnungsService;
import com.kcserver.kjfp.dto.simulation.*;
import com.kcserver.kjfp.enumtype.VeranstaltungTyp;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimulationBerechnungUseCaseTest {
    final PlanungBerechnungService rechnung=new PlanungBerechnungService(new FoerderService(mock(FoerdersatzService.class),mock(KikZuschlagService.class),new AltersService(),new VeranstaltungBerechnungsService()),new VeranstaltungBerechnungsService());
    @Test void vollstaendigesSzenarioWirdOhneGemockteBerechnungDurchgerechnet() {
        var s=PlanungsSimulation.builder().veranstaltung(VeranstaltungsInfo.builder().tage(5).naechte(4).typ(VeranstaltungTyp.JEM).build()).teilnehmer(10).mitarbeiter(2)
            .teilnehmerBeitragUnter21Jahre(new BigDecimal("96")).mitarbeiterBeitrag(new BigDecimal("115"))
            .unterkunftPreisProPersonUndNacht(new BigDecimal("20")).verpflegungPreisProPersonUndTag(new BigDecimal("12"))
            .honorare(new BigDecimal("100")).fahrkosten(new BigDecimal("80")).verbrauchsmaterialProTag(new BigDecimal("3"))
            .kultur(new BigDecimal("40")).miete(new BigDecimal("30")).sonstigeKostenProTag(new BigDecimal("2")).build();
        var ergebnis=new SimulationEngine(rechnung).simuliere(s);
        // Unterkunft 960, Verpflegung 720, übrige Kosten 275; Beiträge 1190, Förderung 500.
        assertThat(ergebnis.getKosten()).isEqualByComparingTo("1955");assertThat(ergebnis.getEinnahmen()).isEqualByComparingTo("1690");assertThat(ergebnis.getSaldo()).isEqualByComparingTo("-265");
        assertThat(ergebnis.getDurchschnittlicherPersonenbeitrag()).isEqualByComparingTo("99.17");assertThat(ergebnis.getEmpfohlenerPersonenbeitrag()).isEqualByComparingTo("100");
        var v=ergebnis.getBeitragsVorschlag();assertThat(v.teilnehmerBeitragUnter21Jahre()).isEqualByComparingTo("95");assertThat(v.mitarbeiterBeitrag()).isEqualByComparingTo("125");assertThat(v.durchschnittlicherPersonenbeitrag()).isEqualByComparingTo("100");
    }
    @Test void ohnePersonenIstAuchDerVorschlagNullUndEsGibtKeineDivisionDurchNull() {
        var s=PlanungsSimulation.builder().veranstaltung(VeranstaltungsInfo.builder().tage(1).naechte(0).typ(VeranstaltungTyp.FM).build()).build();
        assertThat(rechnung.berechneBeitragsVorschlag(s).durchschnittlicherPersonenbeitrag()).isZero();assertThat(rechnung.berechneDurchschnittlichenPersonenbeitrag(s)).isZero();
    }
    @Test void tagesveranstaltungHatKeineUnterkunftUndLangeMassnahmenBegrenzenTagessaetze() {
        var s=PlanungsSimulation.builder().veranstaltung(VeranstaltungsInfo.builder().tage(1).naechte(0).build()).teilnehmer(2).unterkunftPreisProPersonUndNacht(BigDecimal.TEN).verpflegungPreisProPersonUndTag(BigDecimal.TEN).verbrauchsmaterialProTag(BigDecimal.ONE).build();
        assertThat(rechnung.berechneUnterkunft(s)).isZero();assertThat(rechnung.berechneVerpflegung(s)).isEqualByComparingTo("20");
        s.getVeranstaltung().setTage(22);s.getVeranstaltung().setNaechte(21);
        assertThat(rechnung.berechneUnterkunft(s)).isEqualByComparingTo("420");assertThat(rechnung.berechneVerpflegung(s)).isEqualByComparingTo("420");assertThat(rechnung.berechneVerbrauchsmaterial(s)).isEqualByComparingTo("21");
    }
}
