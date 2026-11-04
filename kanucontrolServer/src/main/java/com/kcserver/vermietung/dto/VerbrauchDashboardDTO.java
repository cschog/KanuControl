package com.kcserver.vermietung.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VerbrauchDashboardDTO(int jahr, Long mietobjektId, String mietobjekt,
        LocalDateTime stand, List<ZaehlerAuswertung> zaehler) {
    public record Kennzahlen(BigDecimal gesamt, BigDecimal mitBuchung, BigDecimal ohneBuchung,
        BigDecimal pro24hMit, BigDecimal pro24hOhne, BigDecimal abgedeckteStunden,
        BigDecimal zeitraumStunden, BigDecimal abdeckungProzent) {}
    public record ZaehlerAuswertung(Long id, String bezeichnung, String verbrauchsart, String einheit,
        boolean aktiv, boolean gemeinsam, LocalDateTime letzteAblesung, Kennzahlen kennzahlen,
        List<Monat> monate, List<Intervall> intervalle, List<Belegung> buchungen,
        Korrelation korrelation, List<String> hinweise) {}
    public record Monat(int monat, BigDecimal verbrauch, BigDecimal mitBuchung, BigDecimal ohneBuchung,
        BigDecimal belegungsquote, BigDecimal abgedeckteStunden, boolean geschaetzt) {}
    public record Intervall(LocalDateTime von, LocalDateTime bis, LocalDateTime ablesungVon,
        LocalDateTime ablesungBis, BigDecimal verbrauch, BigDecimal mitBuchung, BigDecimal ohneBuchung,
        BigDecimal belegungsquote, boolean geschaetzt, List<String> buchungen) {}
    public record Belegung(Long id, String nummer, String status, LocalDateTime von, LocalDateTime bis) {}
    public record Punkt(LocalDateTime von, LocalDateTime bis, double belegungsquote, double pro24h) {}
    public record Korrelation(Double pearson, int intervalle, List<Punkt> punkte) {}
}
