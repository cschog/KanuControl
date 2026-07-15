package com.kcserver.dto.beitrag;

import java.math.BigDecimal;

public record BeitragsVorschlag(
        BigDecimal teilnehmerBeitragUnter21Jahre,
        BigDecimal mitarbeiterBeitrag,
        BigDecimal durchschnittlicherPersonenbeitrag
) {
}
