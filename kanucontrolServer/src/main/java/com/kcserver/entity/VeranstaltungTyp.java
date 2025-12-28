package com.kcserver.entity;
/**
 * Veranstaltungstypen gemäß Förderrichtlinie
 */
public enum VeranstaltungTyp {
    JEM("Jugenderholungsmaßnahme", 21),
    FM("Freizeitmaßnahme", 4),
    B("Bildungsmaßnahme", null),       // später
    G("Großveranstaltung", null);      // später

    private final String bezeichnung;
    private final Integer maxTage;

    VeranstaltungTyp(String bezeichnung, Integer maxTage) {
        this.bezeichnung = bezeichnung;
        this.maxTage = maxTage;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public Integer getMaxTage() {
        return maxTage;
    }
}
