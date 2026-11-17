package com.kcserver.kjfp.usecase;

import com.kcserver.kjfp.service.simulation.SimulationFacade;
import com.kcserver.kjfp.service.planung.PlanungService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

@Transactional
class SimulationSpeicherungUseCaseTest extends KjfpUseCaseBasis {
    @Autowired SimulationFacade simulation;
    @Autowired PlanungService planung;
    @Test void entwurfBleibtLokalBisGespeichertUndEinreichungSperrtWeitereSpeicherung() {
        var v=event();var id=v.getId();var s=simulation.getSimulation(id);s.setTeilnehmer(12);s.setMitarbeiter(3);s.setTeilnehmerBeitragUnter21Jahre(new BigDecimal("75"));s.setMitarbeiterBeitrag(new BigDecimal("100"));s.setKultur(new BigDecimal("123.45"));s.setKikZertifiziert(true);
        assertThat(simulation.getSimulation(id).getKultur()).isNotEqualByComparingTo("123.45");
        simulation.saveSimulation(id,s);neuLaden();var gespeichert=simulation.getSimulation(id);
        assertThat(gespeichert.getTeilnehmer()).isEqualTo(12);assertThat(gespeichert.getMitarbeiter()).isEqualTo(3);assertThat(gespeichert.getKultur()).isEqualByComparingTo("123.45");assertThat(gespeichert.isKikZertifiziert()).isTrue();assertThat(gespeichert.getTeilnehmerBeitragUnter21Jahre()).isEqualByComparingTo("75");assertThat(gespeichert.getMitarbeiterBeitrag()).isEqualByComparingTo("100");
        planung.einreichen(id);em.flush();gespeichert.setKultur(BigDecimal.TEN);
        assertThatThrownBy(()->simulation.saveSimulation(id,gespeichert)).hasMessageContaining("409");
    }
    @Test void nachWiederoeffnenKannEinePlanungErneutGespeichertWerden() {
        var id=event().getId();var s=simulation.getSimulation(id);s.setTeilnehmer(20);s.setMitarbeiter(4);s.setKultur(new BigDecimal("500"));simulation.saveSimulation(id,s);planung.einreichen(id);planung.wiederOeffnen(id);s.setFahrkosten(new BigDecimal("99"));simulation.saveSimulation(id,s);neuLaden();assertThat(simulation.getSimulation(id).getFahrkosten()).isEqualByComparingTo("99");
    }
    @Test void fehlendeVoraussetzungenErgebenVerstaendlicheFachfehler() {
        var v=event();v.setUnterkunftsart(null);v.setVerpflegungsmodell(null);v.setBeitragsstruktur(null);em.flush();
        assertThatThrownBy(()->simulation.getSimulation(v.getId())).isInstanceOf(com.kcserver.kjfp.exception.SimulationVoraussetzungenException.class);
    }
}
