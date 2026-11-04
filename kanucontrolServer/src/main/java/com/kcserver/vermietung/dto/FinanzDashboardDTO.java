package com.kcserver.vermietung.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
public record FinanzDashboardDTO(int jahr, LocalDate stand, BigDecimal umsatzgrenze,
    BigDecimal umsatz, BigDecimal vorjahresumsatz, BigDecimal verbleibend,
    BigDecimal geplantOffen, BigDecimal buchungsprognose, BigDecimal trendprognose,
    BigDecimal prognose, int unbefristeteBuchungen, List<Monat> monate, List<Buchung> buchungen,
    List<Quelle> quellen) {
    public record Monat(int monat, BigDecimal umsatz, BigDecimal kumuliert, BigDecimal vorjahrKumuliert) {}
    public record Buchung(Long id, String nummer, String mieter, String mietobjekt, String quelle, String status,
        BigDecimal umsatz, BigDecimal geplantOffen) {}
    public record Quelle(String quelle, BigDecimal umsatz) {}
}
