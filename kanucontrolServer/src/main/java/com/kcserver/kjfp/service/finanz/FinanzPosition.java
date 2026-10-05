package com.kcserver.kjfp.service.finanz;

import com.kcserver.kjfp.enumtype.FinanzKategorie;
import java.math.BigDecimal;

public interface FinanzPosition {

    FinanzKategorie getKategorie();

    BigDecimal getBetrag();
}