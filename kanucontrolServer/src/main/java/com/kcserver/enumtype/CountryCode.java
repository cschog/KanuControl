package com.kcserver.enumtype;

/**
 * ISO 3166-1 alpha-2 country codes (Europe)
 */
public enum CountryCode implements CodeEnum {

    AL, AD, AT, BE, BA, BG, HR, CY, CZ, DK, EE, FI, FR, DE,
    GR, HU, IS, IE, IT, LV, LI, LT, LU, MT, MC, ME, NL, MK,
    NO, PL, PT, RO, SM, RS, SK, SI, ES, SE, CH, TR, UA, VA, GB;

    @Override
    public String getCode() {
        return name(); // ← ISO-Code direkt aus Enum-Namen
    }
}