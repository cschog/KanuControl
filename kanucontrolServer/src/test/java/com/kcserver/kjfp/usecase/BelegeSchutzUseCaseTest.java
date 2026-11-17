package com.kcserver.kjfp.usecase;

import com.kcserver.kjfp.dto.abrechnung.*;
import com.kcserver.kjfp.enumtype.*;
import com.kcserver.kjfp.repository.abrechnung.AbrechnungBelegRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

@Transactional
class BelegeSchutzUseCaseTest extends KjfpUseCaseBasis {
    @Autowired AbrechnungBelegRepository belege;
    AbrechnungBuchungCreateDTO position(String anteil) {
        var dto=new AbrechnungBuchungCreateDTO();dto.setKategorie(FinanzKategorie.KULTUR);dto.setBetrag(new BigDecimal("100"));dto.setKjfpBetrag(new BigDecimal(anteil));return dto;
    }
    AbrechnungBelegCreateDTO kopf(){var dto=new AbrechnungBelegCreateDTO();dto.setDatum(START);dto.setKuerzel("A");return dto;}
    @ParameterizedTest @ValueSource(strings={"kopf","positionAnlegen","positionAendern","positionLoeschen","belegLoeschen","kuerzel"})
    void abgeschlosseneAbrechnungSperrtAlleManuellenSchreibwege(String aktion) {
        var id=event().getId();finanzGruppeService.create(id,"A");var abrechnung=createOpenAbrechnung(id);var beleg=abrechnungBelegService.createBeleg(id,kopf());var pos=abrechnungBelegService.addPosition(id,beleg.getId(),position("50"));
        abrechnung.setStatus(AbrechnungsStatus.ABGESCHLOSSEN);em.flush();
        assertThatThrownBy(()->{
            switch(aktion) {
                case "kopf" -> abrechnungBelegService.updateBeleg(id,beleg.getId(),kopf());
                case "positionAnlegen" -> abrechnungBelegService.addPosition(id,beleg.getId(),position("50"));
                case "positionAendern" -> abrechnungBelegService.updatePosition(id,pos.getId(),position("50"));
                case "positionLoeschen" -> abrechnungBelegService.deletePosition(id,pos.getId());
                case "belegLoeschen" -> abrechnungBelegService.deleteBeleg(id,beleg.getId());
                case "kuerzel" -> abrechnungBelegService.changeKuerzel(id,beleg.getId(),"A");
            }
        }).hasMessageContaining("409");
    }
    @ParameterizedTest @ValueSource(strings={"-0.01","100.01"})
    void kjfpTeilbetragMussInnerhalbDesGesamtbetragsLiegen(String anteil) {
        var id=event().getId();finanzGruppeService.create(id,"A");createOpenAbrechnung(id);var beleg=abrechnungBelegService.createBeleg(id,kopf());
        assertThatThrownBy(()->abrechnungBelegService.addPosition(id,beleg.getId(),position(anteil))).hasMessageContaining("400");
    }
    @Test void systembelegeDuerfenNichtManuellBearbeitetWerden() {
        var id=event().getId();var abrechnung=createOpenAbrechnung(id);var system=belege.findByAbrechnungAndBelegnummer(abrechnung,BuchungsHerkunft.TEILNEHMERBEITRAG.getBelegnummer()).orElseThrow();
        assertThatThrownBy(()->abrechnungBelegService.addPosition(id,system.getId(),position("50"))).hasMessageContaining("409");
    }
    @Test void fremderVeranstaltungskontextWirdAbgewiesen() {
        var id=event().getId();finanzGruppeService.create(id,"A");createOpenAbrechnung(id);var beleg=abrechnungBelegService.createBeleg(id,kopf());var fremd=event().getId();
        assertThatThrownBy(()->abrechnungBelegService.updateBeleg(fremd,beleg.getId(),kopf())).hasMessageContaining("409");
    }
}
