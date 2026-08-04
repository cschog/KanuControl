package com.kcserver.service.finanz;

import com.kcserver.enumtype.FinanzKategorie;
import java.math.BigDecimal;

public interface FinanzPosition {

    FinanzKategorie getKategorie();

    BigDecimal getBetrag();
}