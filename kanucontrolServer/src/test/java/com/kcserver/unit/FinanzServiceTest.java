package com.kcserver.unit;

import com.kcserver.dto.finanz.FinanzSummaryDTO;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.finanz.FinanzPosition;
import com.kcserver.finanz.FinanzService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FinanzServiceTest {

    private FinanzService service;

    @BeforeEach
    void setUp() {
        service = new FinanzService();
    }

    /* =========================================================
       TEST SUM KOSTEN
       ========================================================= */

    @Test
    void sumKosten_shouldReturnCorrectSum() {

        List<FinanzPosition> list = List.of(
                new TestPosition(FinanzKategorie.UNTERKUNFT, 100),
                new TestPosition(FinanzKategorie.VERPFLEGUNG, 50),
                new TestPosition(FinanzKategorie.TEILNEHMERBEITRAG, 200) // Einnahme
        );

        BigDecimal result = service.sumKosten(list);

        assertEquals(0, result.compareTo(new BigDecimal("150.00")));
    }

    /* =========================================================
       TEST SUM EINNAHMEN
       ========================================================= */

    @Test
    void sumEinnahmen_shouldReturnCorrectSum() {

        List<FinanzPosition> list = List.of(
                new TestPosition(FinanzKategorie.UNTERKUNFT, 100),
                new TestPosition(FinanzKategorie.TEILNEHMERBEITRAG, 200),
                new TestPosition(FinanzKategorie.PFAND, 50)
        );

        BigDecimal result = service.sumEinnahmen(list);

        assertEquals(0, result.compareTo(new BigDecimal("250.00")));
    }

    /* =========================================================
       TEST SALDO
       ========================================================= */

    @Test
    void saldo_shouldReturnCorrectDifference() {

        List<FinanzPosition> list = List.of(
                new TestPosition(FinanzKategorie.UNTERKUNFT, 100),
                new TestPosition(FinanzKategorie.TEILNEHMERBEITRAG, 150)
        );

        BigDecimal saldo = service.saldo(list);

        assertEquals(0, saldo.compareTo(new BigDecimal("50.00")));
    }

    /* =========================================================
       TEST VALIDATE AUSGEGLICHEN - OK
       ========================================================= */

    @Test
    void validateAusgeglichen_shouldPass_whenBalanced() {

        List<FinanzPosition> list = List.of(
                new TestPosition(FinanzKategorie.UNTERKUNFT, 100),
                new TestPosition(FinanzKategorie.TEILNEHMERBEITRAG, 100)
        );

        assertDoesNotThrow(() -> service.validateAusgeglichen(list));
    }

    /* =========================================================
       TEST VALIDATE AUSGEGLICHEN - ERROR
       ========================================================= */

    @Test
    void validateAusgeglichen_shouldThrow_whenNotBalanced() {

        List<FinanzPosition> list = List.of(
                new TestPosition(FinanzKategorie.UNTERKUNFT, 100),
                new TestPosition(FinanzKategorie.TEILNEHMERBEITRAG, 50)
        );

        assertThrows(
                ResponseStatusException.class,
                () -> service.validateAusgeglichen(list)
        );
    }

    /* =========================================================
       TEST NULL BETRAG HANDLING
       ========================================================= */

    @Test
    void sum_shouldHandleNullAmounts() {

        List<FinanzPosition> list = List.of(
                new TestPosition(FinanzKategorie.UNTERKUNFT, null),
                new TestPosition(FinanzKategorie.TEILNEHMERBEITRAG, 100)
        );

        BigDecimal kosten = service.sumKosten(list);
        BigDecimal einnahmen = service.sumEinnahmen(list);

        assertEquals(0, kosten.compareTo(new BigDecimal("0.00")));
        assertEquals(0, einnahmen.compareTo(new BigDecimal("100.00")));
    }

    /* =========================================================
       TEST EMPTY LIST
       ========================================================= */

    @Test
    void emptyList_shouldReturnZero() {

        BigDecimal kosten = service.sumKosten(List.of());
        BigDecimal einnahmen = service.sumEinnahmen(List.of());

        assertEquals(0, kosten.compareTo(new BigDecimal("0.00")));
        assertEquals(0, einnahmen.compareTo(new BigDecimal("0.00")));
    }

    /* =========================================================
       TEST BUILD SUMMARY
       ========================================================= */

    @Test
    void buildSummary_shouldCalculateAllValues() {

        List<FinanzPosition> list = List.of(
                new TestPosition(FinanzKategorie.UNTERKUNFT, 200),
                new TestPosition(FinanzKategorie.TEILNEHMERBEITRAG, 300)
        );

        FinanzSummaryDTO summary =
                service.buildSummary(list, 10);

        assertEquals(0,
                summary.getKosten().compareTo(new BigDecimal("200.00")));

        assertEquals(0,
                summary.getEinnahmen().compareTo(new BigDecimal("300.00")));

        assertEquals(0,
                summary.getSaldo().compareTo(new BigDecimal("100.00")));

        assertEquals(0,
                summary.getTeilnehmerKostenProPerson()
                        .compareTo(new BigDecimal("20.00")));
    }

    /* =========================================================
       TEST HELPER CLASS
       ========================================================= */

    private static class TestPosition implements FinanzPosition {

        private final FinanzKategorie kategorie;
        private final BigDecimal betrag;

        TestPosition(FinanzKategorie kategorie, Integer betrag) {

            this.kategorie = kategorie;

            this.betrag = betrag == null
                    ? null
                    : new BigDecimal(betrag).setScale(2);
        }

        @Override
        public FinanzKategorie getKategorie() {
            return kategorie;
        }

        @Override
        public BigDecimal getBetrag() {
            return betrag;
        }
    }
}