package com.kcserver.finanz;

import com.kcserver.kjfp.enumtype.Zahlungsstatus;
import com.kcserver.kjfp.service.zahlungsnachweis.ZahlungsstatusService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ZahlungsstatusServiceTest {
    private final ZahlungsstatusService service = new ZahlungsstatusService(null);

    @ParameterizedTest
    @CsvSource(value = {
            "0, null, GRUEN",
            "0.00, 0.00, GRUEN",
            "0.000, 10.00, GRUEN",
            "100.00, null, ROT",
            "100.00, 0.00, ROT",
            "100.00, 25.00, GELB",
            "100.00, 100.00, GRUEN",
            "100.00, 120.00, GRUEN"
    }, nullValues = "null")
    void calculatesStatusFromCurrentDueAndActualPayments(
            BigDecimal due, BigDecimal paid, Zahlungsstatus expected) {
        assertThat(service.getStatus(due, paid)).isEqualTo(expected);
    }
}
