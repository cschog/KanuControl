package com.kcserver.kjfp.enumtype;

/**
 * ISO 3166-1 alpha-2 country codes (Europe)
 */
public enum CountryCode implements CodeEnum {

    AL("Albanien"),
    AD("Andorra"),
    AT("Österreich"),
    BE("Belgien"),
    BA("Bosnien und Herzegowina"),
    BG("Bulgarien"),
    HR("Kroatien"),
    CY("Zypern"),
    CZ("Tschechien"),
    DK("Dänemark"),
    EE("Estland"),
    FI("Finnland"),
    FR("Frankreich"),
    DE("Deutschland"),
    GR("Griechenland"),
    HU("Ungarn"),
    IS("Island"),
    IE("Irland"),
    IT("Italien"),
    LV("Lettland"),
    LI("Liechtenstein"),
    LT("Litauen"),
    LU("Luxemburg"),
    MT("Malta"),
    MC("Monaco"),
    ME("Montenegro"),
    NL("Niederlande"),
    MK("Nordmazedonien"),
    NO("Norwegen"),
    PL("Polen"),
    PT("Portugal"),
    RO("Rumänien"),
    SM("San Marino"),
    RS("Serbien"),
    SK("Slowakei"),
    SI("Slowenien"),
    ES("Spanien"),
    SE("Schweden"),
    CH("Schweiz"),
    TR("Türkei"),
    UA("Ukraine"),
    VA("Vatikanstadt"),
    GB("Vereinigtes Königreich");

    private final String label;

    CountryCode(String label) {
        this.label = label;
    }

    @Override
    public String getCode() {
        return name();   // 🔒 bleibt exakt gleich
    }

    public String getLabel() {
        return label;    // ➕ neue Funktion
    }
}