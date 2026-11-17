package com.kcserver.kjfp.usecase;

import com.kcserver.kjfp.service.zahlungsnachweis.ZahlungsnachweisService;
import com.kcserver.kjfp.service.abrechnung.AbrechnungSynchronisationsService;
import com.kcserver.kjfp.service.finanz.*;
import com.kcserver.kjfp.service.simulation.SimulationFacade;
import com.kcserver.kjfp.dto.abrechnung.*;
import com.kcserver.kjfp.enumtype.*;
import com.kcserver.kjfp.repository.abrechnung.*;
import com.kcserver.kjfp.repository.fahrkosten.ReisekostenabrechnungRepository;
import com.kcserver.kjfp.entity.fahrkosten.Reisekostenabrechnung;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

@Transactional
class FinanzenGesamtUseCaseTest extends KjfpUseCaseBasis {
    @Autowired ZahlungsnachweisService zahlungen;
    @Autowired FinanzausgleichService ausgleich;
    @Autowired FinanzenDashboardService dashboard;
    @Autowired SimulationFacade simulation;
    @Autowired AbrechnungSynchronisationsService sync;
    @Autowired AbrechnungBuchungRepository buchungen;
    @Autowired ReisekostenabrechnungRepository reisen;
    void beleg(Long event,String konto,FinanzKategorie kategorie,String betrag) {
        var b=new AbrechnungBelegCreateDTO();b.setKuerzel(konto);b.setDatum(START);b.setBeschreibung("Fachlicher Test");
        var pos=new AbrechnungBuchungCreateDTO();pos.setKategorie(kategorie);pos.setBetrag(new BigDecimal(betrag));abrechnungBelegService.createBelegMitBuchung(event,b,pos);
    }
    @Test void zweiKontenVkZahlungswegeRueckzahlungFahrkostenUndDashboardBleibenKonsistent() {
        var v=event();var id=v.getId();var a=finanzGruppeService.create(id,"A");var b=finanzGruppeService.create(id,"B");var ta=person(v,a);var tb=person(v,b);em.flush();
        var s=simulation.getSimulation(id);s.setTeilnehmer(2);s.setMitarbeiter(0);s.setTeilnehmerBeitragUnter21Jahre(new BigDecimal("100"));s.setUnterkunftPreisProPersonUndNacht(BigDecimal.TEN);s.setVerpflegungPreisProPersonUndTag(BigDecimal.ZERO);s.setVerbrauchsmaterialProTag(BigDecimal.ZERO);s.setSonstigeKostenProTag(BigDecimal.ZERO);simulation.saveSimulation(id,s);
        createOpenAbrechnung(id);
        var za=zahlungen.create(id,zahlung("120",Zahlungsweg.UEBERWEISUNG,a,ta));
        zahlungen.rueckzahlungTeilnehmerbeitrag(id,za.getId(),new BigDecimal("20"),null,Zahlungsweg.UEBERWEISUNG);
        var zb=zahlungen.create(id,zahlung("110",Zahlungsweg.QUITTUNG,b,tb));
        zahlungen.rueckzahlungTeilnehmerbeitrag(id,zb.getId(),BigDecimal.TEN,null,Zahlungsweg.QUITTUNG);
        beleg(id,"A",FinanzKategorie.KULTUR,"40");beleg(id,"A",FinanzKategorie.PFAND,"5");beleg(id,"B",FinanzKategorie.UNTERKUNFT,"30");
        var fahrt=new Reisekostenabrechnung();fahrt.setVeranstaltung(v);fahrt.setFahrer(ta.getPerson());fahrt.setFinanzGruppe(a);fahrt.setAbrechnungsdatum(START);fahrt.setGesamtKilometer(100);fahrt.setGesamtBetrag(new BigDecimal("20"));reisen.save(fahrt);
        neuLaden();
        var rows=ausgleich.berechne(id);var ra=rows.stream().filter(r->r.getFinanzGruppeKuerzel().equals("A")).findFirst().orElseThrow();var rb=rows.stream().filter(r->r.getFinanzGruppeKuerzel().equals("B")).findFirst().orElseThrow();var vk=rows.stream().filter(r->r.getFinanzGruppeKuerzel().equals("VK")).findFirst().orElseThrow();
        assertThat(ra.getTeilnehmerBeitraegeUeberweisung()).isEqualByComparingTo("100");assertThat(ra.getAusgaben()).isEqualByComparingTo("35");assertThat(ra.getFahrkosten()).isEqualByComparingTo("20");assertThat(ra.getErstattungVomVK()).isEqualByComparingTo("55");
        assertThat(rb.getTeilnehmerBeitraegeQuittung()).isEqualByComparingTo("100");assertThat(rb.getErstattungVomVK()).isEqualByComparingTo("-70");assertThat(vk.getTeilnehmerBeitraegeUeberweisung()).isEqualByComparingTo("100");
        var pruefung=ausgleich.pruefeTeilnehmerBeitraege(id);assertThat(pruefung.getGesamtSoll()).isEqualByComparingTo("200");assertThat(pruefung.getGesamtIst()).isEqualByComparingTo("200");assertThat(pruefung.getStatus()).isEqualTo(FinanzausgleichGesamtstatus.OK);
        var d=dashboard.getDashboard(id);neuLaden();assertThat(d.getPlanKosten()).isEqualByComparingTo("80");assertThat(d.getPlanEinnahmen()).isEqualByComparingTo("300");assertThat(d.getIstKosten()).isEqualByComparingTo("90");
        // 230 eingegangen minus 30 Erstattung + 5 Pfand; ältere Teilnehmer erhalten keinen Zuschuss.
        assertThat(d.getIstEinnahmen()).isEqualByComparingTo("205");assertThat(d.getIstSaldo()).isEqualByComparingTo("115");
        sync.synchronisieren(id);sync.synchronisieren(id);neuLaden();
        var wieder=dashboard.getDashboard(id);assertThat(wieder.getIstEinnahmen()).isEqualByComparingTo("205");assertThat(wieder.getIstKosten()).isEqualByComparingTo("90");
        var abr=abrechnungRepository.findByVeranstaltungId(id).orElseThrow();assertThat(buchungen.findByBeleg_Abrechnung_Id(abr.getId()).stream().filter(p->p.getHerkunft()==BuchungsHerkunft.TEILNEHMERBEITRAG)).hasSize(1);
        veranstaltungRepository.findById(id).orElseThrow().getBeitragsstruktur().getRegeln().getFirst().setBeitrag(new BigDecimal("150"));neuLaden();
        assertThat(ausgleich.pruefeTeilnehmerBeitraege(id).getStatus()).isEqualTo(FinanzausgleichGesamtstatus.BEITRAEGE_FEHLEN);
    }
    @Test void geloeschteZahlungEntferntDieAutomatischeBuchungUndErneuteZahlungWirdNurEinmalGezaehlt() {
        var v=event();var id=v.getId();var konto=finanzGruppeService.create(id,"A");var t=person(v,konto);em.flush();createOpenAbrechnung(id);
        var zahlung=zahlungen.create(id,zahlung("50",Zahlungsweg.UEBERWEISUNG,konto,t));neuLaden();
        assertThat(dashboard.getDashboard(id).getIstEinnahmen()).isEqualByComparingTo("50");neuLaden();
        zahlungen.delete(id,zahlung.getId());neuLaden();
        assertThat(dashboard.getDashboard(id).getIstEinnahmen()).isZero();neuLaden();
        zahlungen.create(id,zahlung("25",Zahlungsweg.UEBERWEISUNG,konto,t));neuLaden();
        assertThat(dashboard.getDashboard(id).getIstEinnahmen()).isEqualByComparingTo("25");neuLaden();
        assertThat(dashboard.getDashboard(id).getIstEinnahmen()).isEqualByComparingTo("25");
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({"100,100,OK","150,0,BEITRAEGE_FEHLEN","210,0,UEBERZAHLUNG","200,0,FINANZGRUPPEN_ABWEICHUNG"})
    void pruefstatusUnterscheidetFehlbetragUeberzahlungUndFalscheKontoverteilung(String betragA,String betragB,FinanzausgleichGesamtstatus erwartet) {
        var v=event();var id=v.getId();var a=finanzGruppeService.create(id,"A");var b=finanzGruppeService.create(id,"B");var ta=person(v,a);var tb=person(v,b);em.flush();
        zahlungen.create(id,zahlung(betragA,Zahlungsweg.UEBERWEISUNG,a,ta));
        if(new BigDecimal(betragB).signum()>0)zahlungen.create(id,zahlung(betragB,Zahlungsweg.QUITTUNG,b,tb));
        neuLaden();assertThat(ausgleich.pruefeTeilnehmerBeitraege(id).getStatus()).isEqualTo(erwartet);
    }

}
