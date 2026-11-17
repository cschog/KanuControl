package com.kcserver.kjfp.usecase;

import com.kcserver.kjfp.service.zahlungsnachweis.*;
import com.kcserver.kjfp.repository.zahlungsnachweis.ZahlungsnachweisRepository;
import com.kcserver.kjfp.enumtype.Zahlungsweg;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

@Transactional
class ZahlungenUseCaseTest extends KjfpUseCaseBasis {
    @Autowired ZahlungsnachweisService zahlungen;
    @Autowired ZahlungsnachweisRepository repository;
    @Autowired ZahlungsnachweisSaldoService salden;
    @Test void sammelzahlungCentRestBearbeitenTeilnehmerwechselUndLoeschenWerdenGespeichert() {
        var v=event();var konto=finanzGruppeService.create(v.getId(),"A");var a=person(v,konto);var b=person(v,konto);var c=person(v,konto);em.flush();
        var id=zahlungen.create(v.getId(),zahlung("100",Zahlungsweg.UEBERWEISUNG,konto,a,b,c)).getId();neuLaden();
        var z=repository.findById(id).orElseThrow();
        assertThat(z.getPositionen()).extracting(p->p.getBetrag()).containsExactlyInAnyOrder(new BigDecimal("33.33"),new BigDecimal("33.33"),new BigDecimal("33.34"));
        assertThat(salden.getZugeordnetenBetrag(z)).isEqualByComparingTo("100");
        zahlungen.update(v.getId(),id,zahlung("120",Zahlungsweg.UEBERWEISUNG,konto,a,b));neuLaden();
        z=repository.findById(id).orElseThrow();assertThat(z.getPositionen()).hasSize(2).allSatisfy(p->assertThat(p.getBetrag()).isEqualByComparingTo("60"));
        assertThat(repository.sumBetragByTeilnehmerId(c.getId(),null)).isZero();
        zahlungen.update(v.getId(),id,zahlung("120",Zahlungsweg.UEBERWEISUNG,konto,a,b));neuLaden();
        assertThat(repository.sumBetragByTeilnehmerId(a.getId(),null)).isEqualByComparingTo("60");
        zahlungen.delete(v.getId(),id);neuLaden();assertThat(repository.findById(id)).isEmpty();assertThat(repository.sumBetragByTeilnehmerId(a.getId(),null)).isZero();
    }
    @Test void ueberzahlungWirdSchrittweiseErstattetOhneDieTeilnehmerbeitraegeZuVeraendern() {
        var v=event();var konto=finanzGruppeService.create(v.getId(),"A");var t=person(v,konto);em.flush();
        var id=zahlungen.create(v.getId(),zahlung("150",Zahlungsweg.QUITTUNG,konto,t)).getId();neuLaden();
        assertThat(salden.getOffeneUeberzahlung(repository.findById(id).orElseThrow())).isEqualByComparingTo("50");
        var rueck=zahlungen.rueckzahlungTeilnehmerbeitrag(v.getId(),id,new BigDecimal("20"),"Teilrückzahlung",Zahlungsweg.UEBERWEISUNG);neuLaden();
        var r=repository.findById(rueck.getId()).orElseThrow();assertThat(r.getUrspruenglicherZahlungsnachweis().getId()).isEqualTo(id);assertThat(r.getPositionen()).isEmpty();
        assertThat(salden.getOffeneUeberzahlung(repository.findById(id).orElseThrow())).isEqualByComparingTo("30");
        zahlungen.rueckzahlungTeilnehmerbeitrag(v.getId(),id,new BigDecimal("30"),null,Zahlungsweg.QUITTUNG);neuLaden();
        assertThat(salden.getOffeneUeberzahlung(repository.findById(id).orElseThrow())).isZero();
        assertThat(repository.sumBetragByTeilnehmerId(t.getId(),null)).isEqualByComparingTo("100");
        assertThatThrownBy(()->zahlungen.rueckzahlungTeilnehmerbeitrag(v.getId(),id,BigDecimal.ONE,null,Zahlungsweg.QUITTUNG)).hasMessageContaining("409");
    }
    @Test void zuHoheRueckzahlungIstEinKonflikt() {
        var v=event();var konto=finanzGruppeService.create(v.getId(),"A");var t=person(v,konto);em.flush();
        var id=zahlungen.create(v.getId(),zahlung("110",Zahlungsweg.QUITTUNG,konto,t)).getId();neuLaden();
        assertThatThrownBy(()->zahlungen.rueckzahlungTeilnehmerbeitrag(v.getId(),id,new BigDecimal("10.01"),null,Zahlungsweg.QUITTUNG)).hasMessageContaining("409");
    }
    @Test void fremdeTeilnehmerWerdenNichtEinerZahlungUntergeschoben() {
        var v=event();var fremd=event();var konto=finanzGruppeService.create(fremd.getId(),"B");var t=person(fremd,konto);em.flush();
        assertThatThrownBy(()->zahlungen.create(v.getId(),zahlung("10",Zahlungsweg.UEBERWEISUNG,null,t))).hasMessageContaining("409");
    }
}
