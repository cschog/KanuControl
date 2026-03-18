package com.kcserver.integration;

import com.kcserver.entity.FinanzGruppe;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.finanz.FinanzGruppeService;
import com.kcserver.repository.FinanzGruppeRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
class FinanzGruppeIntegrationTest extends AbstractFinanzIntegrationTest {

    @Autowired
    FinanzGruppeService finanzGruppeService;

    @Autowired
    FinanzGruppeRepository finanzGruppeRepository;

    @Autowired
    TeilnehmerRepository teilnehmerRepository;

    @Autowired
    VeranstaltungRepository veranstaltungRepository;

    @Test
    void assignKuerzel_shouldCreateAndAssign() {

        Long veranstaltungId = createTestVeranstaltung();
        Veranstaltung v = veranstaltungRepository.findById(veranstaltungId).orElseThrow();

        Teilnehmer t = createTeilnehmer(v, BigDecimal.TEN);

        finanzGruppeService.assignKuerzel(
                veranstaltungId,
                t.getId(),
                "SG"
        );

        Teilnehmer updated =
                teilnehmerRepository.findById(t.getId()).orElseThrow();

        assertThat(updated.getFinanzGruppe()).isNotNull();
        assertThat(updated.getFinanzGruppe().getKuerzel()).isEqualTo("SG");
    }

    @Test
    void assignTeilnehmerBulk_shouldAssignMultiple() {

        Long veranstaltungId = createTestVeranstaltung();
        Veranstaltung v = veranstaltungRepository.findById(veranstaltungId).orElseThrow();

        Teilnehmer t1 = createTeilnehmer(v, BigDecimal.TEN);
        Teilnehmer t2 = createTeilnehmer(v, BigDecimal.ONE);

        FinanzGruppe gruppe =
                finanzGruppeService.create(veranstaltungId, "TEAM");

        finanzGruppeService.assignTeilnehmerBulk(
                veranstaltungId,
                gruppe.getId(),
                List.of(t1.getId(), t2.getId())
        );

        Teilnehmer updated1 =
                teilnehmerRepository.findById(t1.getId()).orElseThrow();

        Teilnehmer updated2 =
                teilnehmerRepository.findById(t2.getId()).orElseThrow();

        assertThat(updated1.getFinanzGruppe()).isEqualTo(gruppe);
        assertThat(updated2.getFinanzGruppe()).isEqualTo(gruppe);
    }

    @Test
    void delete_shouldRemoveGroup() {

        Long veranstaltungId = createTestVeranstaltung();

        FinanzGruppe gruppe =
                finanzGruppeService.create(veranstaltungId, "DEL");

        finanzGruppeService.delete(
                veranstaltungId,
                gruppe.getId()
        );

        assertThat(
                finanzGruppeRepository.findById(gruppe.getId())
        ).isEmpty();
    }
}