package com.kcserver.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyUtil {

    private CurrencyUtil() {
    }

    public static String decimal(BigDecimal value) {

        if (value == null) {
            return "0,00";
        }

        DecimalFormatSymbols symbols =
                new DecimalFormatSymbols(Locale.GERMANY);

        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        DecimalFormat format =
                new DecimalFormat("#,##0.00", symbols);

        return format.format(value);
    }
}