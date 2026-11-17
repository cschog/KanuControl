package com.kcserver.kjfp.usecase;

import com.kcserver.core.dto.veranstaltung.VeranstaltungCreateDTO;
import com.kcserver.core.service.veranstaltung.VeranstaltungService;
import com.kcserver.kjfp.repository.PlanungRepository;
import com.kcserver.kjfp.service.simulation.SimulationFacade;
import com.kcserver.kjfp.enumtype.PlanungsStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

@Transactional
class VeranstaltungKopieSpeicherungUseCaseTest extends KjfpUseCaseBasis {
    @Autowired VeranstaltungService events;
    @Autowired SimulationFacade simulation;
    @Autowired PlanungRepository planungen;
    @Autowired org.springframework.transaction.PlatformTransactionManager txManager;
    @Test void kopieWirdMitEigenerPlanungUndUnabhaengigenPositionenWiederGelesen() {
        var source=event();source.getLeiter().setGeburtsdatum(START.minusYears(30));source.getVerein().setKjfpVerein(true);
        var s=simulation.getSimulation(source.getId());s.setTeilnehmer(14);s.setMitarbeiter(3);s.setKultur(new BigDecimal("123.45"));simulation.saveSimulation(source.getId(),s);em.flush();
        var original=planungen.findByVeranstaltungId(source.getId()).orElseThrow();original.setStatus(PlanungsStatus.EINGEREICHT);
        var dto=new VeranstaltungCreateDTO();dto.setName("Kopie mit Planung");dto.setVereinId(source.getVerein().getId());dto.setLeiterId(source.getLeiter().getId());dto.setBeginnDatum(START.plusYears(1));dto.setEndeDatum(START.plusYears(1).plusDays(9));dto.setBeginnZeit(source.getBeginnZeit());dto.setEndeZeit(source.getEndeZeit());dto.setKjfpAktiv(true);dto.setKopieVonVeranstaltungId(source.getId());dto.setUnterkunftsartId(source.getUnterkunftsart().getId());dto.setVerpflegungsmodellId(source.getVerpflegungsmodell().getId());dto.setBeitragsstrukturId(source.getBeitragsstruktur().getId());
        var id=events.create(dto).data().getId();long sourceId=source.getId(),planId=original.getId();neuLaden();
        var copy=planungen.findByVeranstaltungId(id).orElseThrow();assertThat(copy.getId()).isNotEqualTo(planId);assertThat(copy.getTeilnehmer()).isEqualTo(14);assertThat(copy.getMitarbeiter()).isEqualTo(3);assertThat(copy.getKultur()).isEqualByComparingTo("123.45");assertThat(copy.getStatus()).isEqualTo(PlanungsStatus.IN_BEARBEITUNG);
        assertThat(copy.getPositionen()).allSatisfy(p->{assertThat(p.getId()).isNotNull();assertThat(p.getPlanung().getId()).isEqualTo(copy.getId());});
        copy.setKultur(BigDecimal.TEN);neuLaden();var unchanged=planungen.findByVeranstaltungId(sourceId).orElseThrow();assertThat(unchanged.getKultur()).isEqualByComparingTo("123.45");assertThat(unchanged.getStatus()).isEqualTo(PlanungsStatus.EINGEREICHT);
    }
    @Test void spaeterFehlerBeimPlanSpeichernRolltAuchVeranstaltungUndAktivwechselZurueck() {
        var source=event();source.getLeiter().setGeburtsdatum(START.minusYears(30));source.getVerein().setKjfpVerein(true);simulation.saveSimulation(source.getId(),simulation.getSimulation(source.getId()));em.flush();
        long quelle=source.getId();var dto=new VeranstaltungCreateDTO();dto.setName("Kopie muss zurückrollen");dto.setVereinId(source.getVerein().getId());dto.setLeiterId(source.getLeiter().getId());dto.setBeginnDatum(START.plusYears(1));dto.setEndeDatum(START.plusYears(1).plusDays(4));dto.setBeginnZeit(source.getBeginnZeit());dto.setEndeZeit(source.getEndeZeit());dto.setKjfpAktiv(true);dto.setKopieVonVeranstaltungId(quelle);dto.setUnterkunftsartId(source.getUnterkunftsart().getId());dto.setVerpflegungsmodellId(source.getVerpflegungsmodell().getId());dto.setBeitragsstrukturId(source.getBeitragsstruktur().getId());
        org.springframework.test.context.transaction.TestTransaction.flagForCommit();org.springframework.test.context.transaction.TestTransaction.end();
        var tx=new org.springframework.transaction.support.TransactionTemplate(txManager);
        // Reale Datenbankfehler-Injektion ausschließlich im eigenen lokalen Testschema.
        tx.executeWithoutResult(status->jdbcTemplate.execute("ALTER TABLE "+tenant()+".planung ADD CONSTRAINT kjfp_test_rollback CHECK (veranstaltung_id="+quelle+")"));
        try {
            assertThatThrownBy(()->tx.executeWithoutResult(status->{events.create(dto);em.flush();})).hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class);
            tx.executeWithoutResult(status->{assertThat(veranstaltungRepository.findAll()).hasSize(1);assertThat(veranstaltungRepository.findById(quelle).orElseThrow().isAktiv()).isTrue();assertThat(planungen.findByVeranstaltungId(quelle)).isPresent();});
        }finally{tx.executeWithoutResult(status->jdbcTemplate.execute("ALTER TABLE "+tenant()+".planung DROP CONSTRAINT kjfp_test_rollback"));}
    }

}
