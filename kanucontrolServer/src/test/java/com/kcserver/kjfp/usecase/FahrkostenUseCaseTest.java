package com.kcserver.kjfp.usecase;

import com.kcserver.kjfp.dto.reisekosten.*;
import com.kcserver.kjfp.entity.fahrkosten.ReisekostenKonfiguration;
import com.kcserver.kjfp.repository.fahrkosten.*;
import com.kcserver.kjfp.service.reisekosten.ReisekostenabrechnungService;
import com.kcserver.kjfp.enumtype.CountryCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@Transactional
class FahrkostenUseCaseTest extends KjfpUseCaseBasis {
    @Autowired ReisekostenabrechnungService reisen;
    @Autowired ReisekostenabrechnungRepository repository;
    @Autowired ReisekostenKonfigurationRepository konfiguration;
    FahrtabschnittRequest abschnitt(int nr,int km,boolean anhaenger,List<Long> personen) {
        return new FahrtabschnittRequest(null,nr,"Abschnitt "+nr,"52249","Eschweiler",CountryCode.DE,"52349","Düren",CountryCode.DE,km,anhaenger,personen);
    }
    @Test void abschnitteMitEigenenMitfahrernUndAnhaengerWerdenBerechnetGespeichertUndGeloescht() {
        var v=event();var konto=finanzGruppeService.create(v.getId(),"A");var fahrer=person(v,konto);var a=person(v,konto);var b=person(v,konto);
        konfiguration.deleteAll();konfiguration.flush();var config=new ReisekostenKonfiguration();config.setGueltigVon(START);config.setGueltigBis(START);config.setPkwSatz(new BigDecimal("0.30"));config.setMitfahrerSatz(new BigDecimal("0.02"));config.setAnhaengerSatz(new BigDecimal("0.05"));konfiguration.saveAndFlush(config);
        var id=reisen.create(new ReisekostenabrechnungCreateRequest(v.getId(),fahrer.getPerson().getId(),START,konto.getId(),"Testfahrt"));
        reisen.update(id,new ReisekostenabrechnungUpdateRequest(START,konto.getId(),"Hin/Rück",List.of(),List.of(abschnitt(1,100,true,List.of(a.getPerson().getId(),b.getPerson().getId())),abschnitt(2,50,false,List.of(a.getPerson().getId())))));neuLaden();
        var r=repository.findById(id).orElseThrow();assertThat(r.getGesamtKilometer()).isEqualTo(150);assertThat(r.getGesamtBetrag()).isEqualByComparingTo("55.00");assertThat(r.getFahrtabschnitte()).hasSize(2);
        assertThat(reisen.getReisekostenSumme(v.getId())).isEqualByComparingTo("55.00");
        String nummer=r.getBelegnummer();
        reisen.update(id,new ReisekostenabrechnungUpdateRequest(START,konto.getId(),"Korrektur",List.of(),List.of(abschnitt(1,7,false,List.of()))));neuLaden();
        r=repository.findById(id).orElseThrow();assertThat(r.getGesamtBetrag()).isEqualByComparingTo("2.10");assertThat(r.getGesamtKilometer()).isEqualTo(7);assertThat(r.getFahrtabschnitte()).hasSize(1);assertThat(r.getBelegnummer()).isEqualTo(nummer);
        reisen.delete(id);neuLaden();assertThat(reisen.getReisekostenSumme(v.getId())).isZero();
    }
    @Test void minderjaehrigerFahrerWirdAbgelehnt() {
        var v=event();var konto=finanzGruppeService.create(v.getId(),"A");var fahrer=person(v,konto);fahrer.getPerson().setGeburtsdatum(START.minusYears(18).plusDays(1));em.flush();
        assertThatThrownBy(()->reisen.create(new ReisekostenabrechnungCreateRequest(v.getId(),fahrer.getPerson().getId(),START,konto.getId(),null))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("18");
    }
    @Test void fahrerDarfNichtZugleichMitfahrerSein() {
        var v=event();var konto=finanzGruppeService.create(v.getId(),"A");var fahrer=person(v,konto);em.flush();
        var id=reisen.create(new ReisekostenabrechnungCreateRequest(v.getId(),fahrer.getPerson().getId(),START,konto.getId(),null));
        assertThatThrownBy(()->reisen.update(id,new ReisekostenabrechnungUpdateRequest(START,konto.getId(),null,List.of(),List.of(abschnitt(1,10,false,List.of(fahrer.getPerson().getId())))))) .hasMessageContaining("gleichzeitig");
    }
    @Test void fehlendeKonfigurationWirdNichtAlsNullEuroFahrtGespeichert() {
        var v=event();var konto=finanzGruppeService.create(v.getId(),"A");var fahrer=person(v,konto);konfiguration.deleteAll();konfiguration.flush();
        var id=reisen.create(new ReisekostenabrechnungCreateRequest(v.getId(),fahrer.getPerson().getId(),START,konto.getId(),null));
        assertThatThrownBy(()->reisen.update(id,new ReisekostenabrechnungUpdateRequest(START,konto.getId(),null,List.of(),List.of(abschnitt(1,10,false,List.of()))))).hasMessageContaining("Keine Reisekostenkonfiguration");
    }
    @Test void einFahrerKannNichtZweiFahrzeugenZugeordnetWerden() {
        var v=event();var konto=finanzGruppeService.create(v.getId(),"A");var fahrer=person(v,konto);em.flush();
        var request=new ReisekostenabrechnungCreateRequest(v.getId(),fahrer.getPerson().getId(),START,konto.getId(),null);reisen.create(request);em.flush();
        assertThatThrownBy(()->reisen.create(request)).hasMessageContaining("bereits einem Fahrzeug");
    }
    @Test void fremderMitfahrerWirdAbgelehnt() {
        var v=event();var konto=finanzGruppeService.create(v.getId(),"A");var fahrer=person(v,konto);var fremd=person(event(),null);em.flush();
        var id=reisen.create(new ReisekostenabrechnungCreateRequest(v.getId(),fahrer.getPerson().getId(),START,konto.getId(),null));
        assertThatThrownBy(()->reisen.update(id,new ReisekostenabrechnungUpdateRequest(START,konto.getId(),null,List.of(),List.of(abschnitt(1,10,false,List.of(fremd.getPerson().getId())))))).hasMessageContaining("Mitfahrer muss Teilnehmer");
    }

}
