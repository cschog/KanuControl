package com.kcserver.integration;

import com.kcserver.core.entity.Teilnehmer;
import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.kjfp.service.finanz.FinanzGruppeService;
import com.kcserver.core.repository.TeilnehmerRepository;
import com.kcserver.core.repository.VeranstaltungRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@Transactional
class TeilnehmerKuerzelIntegrationTest extends AbstractFinanzIntegrationTest {

    @Autowired
    FinanzGruppeService finanzGruppeService;

    @Autowired
    VeranstaltungRepository veranstaltungRepository;

    @Autowired
    TeilnehmerRepository teilnehmerRepository;

    @Test
    void assignKuerzel_shouldAssign() {

        // 🔹 1. Saubere Test-Veranstaltung (mit Leiter etc.)
        Long veranstaltungId = createTestVeranstaltung();
        Veranstaltung v = veranstaltungRepository.findById(veranstaltungId)
                .orElseThrow();

        // 🔹 2. Gültigen Teilnehmer erzeugen
        Teilnehmer t = createTeilnehmer(v, BigDecimal.TEN);

        // 🔹 3. Kürzel zuweisen
        finanzGruppeService.assignKuerzel(
                veranstaltungId,
                t.getId(),
                "SG"
        );

        // 🔹 4. Validieren
        Teilnehmer updated =
                teilnehmerRepository.findById(t.getId()).orElseThrow();

        assertThat(updated.getFinanzGruppe()).isNotNull();
        assertThat(updated.getFinanzGruppe().getKuerzel())
                .isEqualTo("SG");
    }
}