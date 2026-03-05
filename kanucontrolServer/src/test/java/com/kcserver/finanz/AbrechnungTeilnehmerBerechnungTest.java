package com.kcserver.finanz;

import com.kcserver.entity.Abrechnung;
import com.kcserver.entity.Veranstaltung;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AbrechnungTeilnehmerBerechnungTest
        extends AbstractFinanzIntegrationTest {

    Long veranstaltungId;

    @BeforeEach
    void setup() {

        veranstaltungId = createTestVeranstaltung();

        createOpenAbrechnung(veranstaltungId);
    }

    @Test
    void shouldCalculateStandardFeeSum() {

        Veranstaltung v = veranstaltungRepository
                .findById(veranstaltungId)
                .orElseThrow();

        v.setIndividuelleGebuehren(false);
        v.setStandardGebuehr(new BigDecimal("50.00"));
        veranstaltungRepository.save(v);

        createTeilnehmer(v, null);
        createTeilnehmer(v, null);

        abrechnungService.berechneTeilnehmerEinnahmen(veranstaltungId);

        Abrechnung a = abrechnungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseThrow();

        assertThat(a.getBuchungen()).hasSize(1);

        assertThat(a.getBuchungen().get(0).getBetrag())
                .isEqualByComparingTo("100.00");
    }
}