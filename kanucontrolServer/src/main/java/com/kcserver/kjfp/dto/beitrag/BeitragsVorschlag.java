package com.kcserver.kjfp.dto.beitrag;

import java.math.BigDecimal;

public record BeitragsVorschlag(
        BigDecimal teilnehmerBeitragUnter21Jahre,
        BigDecimal mitarbeiterBeitrag,
        BigDecimal durchschnittlicherPersonenbeitrag
) {
}
