package com.kcserver.vermietung.dto;

import com.kcserver.vermietung.enumtype.Buchungsquelle;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.enumtype.Rabattart;
import com.kcserver.vermietung.enumtype.RabattwertTyp;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BuchungDTO {

    @jakarta.validation.Valid @jakarta.validation.constraints.Size(max=500)
    private List<EntschaedigungPositionDTO> entschaedigungen;
    private Long abrechnungRevision;
    private Long abschlussRevision;
    private java.math.BigDecimal entschaedigungSumme;
    private Long id;
    private java.time.Instant reservierungMailGesendetAm;
    private String reservierungMailFehler;
    private String buchungsnummer;
    private Buchungsstatus status;

    // Null preserves the selection when an older client updates a booking.
    private com.kcserver.vermietung.enumtype.BankverbindungAuswahl bankverbindungAuswahl;

    private Instant createdAt;

    @NotNull
    private LocalDateTime anreise;

    private LocalDateTime abreise;

    private boolean unbefristet = false;

    @NotNull
    private Long mietobjektId;

    private List<Long> mietbereichIds;

    private List<BuchungMietbereichDTO> positionen;

    private List<MietbereichRefDTO> mietbereiche;

    @NotNull
    private Buchungsquelle buchungsquelle = Buchungsquelle.DIREKT;

    @NotNull
    private Long mieterId;

    private Long veranstalterVereinId;

    /**
     * Interne Bemerkungen zur Buchung bzw. zum Mieter.
     */
    private String bemerkung;

    /**
     * Bewertung des Mieters von 1 bis 5 Sternen.
     */
    @jakarta.validation.constraints.Min(1)
    @jakarta.validation.constraints.Max(5)
    private Integer bewertung;

    @jakarta.validation.constraints.Min(0)
    private Integer anzahlJugendliche;

    @jakarta.validation.constraints.Min(0)
    @jakarta.validation.constraints.Max(120)
    private Integer jugendAlterVon;

    @jakarta.validation.constraints.Min(0)
    @jakarta.validation.constraints.Max(120)
    private Integer jugendAlterBis;

    @jakarta.validation.constraints.Min(0)
    private Integer anzahlErwachsene;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @jakarta.validation.constraints.AssertTrue(message = "Teilnehmerzahlen und Altersspanne der Jugendlichen prüfen.")
    public boolean isTeilnehmerdatenGueltig() {
        if (anzahlJugendliche != null && anzahlJugendliche < 0
                || anzahlErwachsene != null && anzahlErwachsene < 0) return false;
        if (anzahlJugendliche == null || anzahlJugendliche == 0) {
            return jugendAlterVon == null && jugendAlterBis == null;
        }
        return jugendAlterVon != null && jugendAlterBis != null
                && jugendAlterVon >= 0 && jugendAlterVon <= jugendAlterBis
                && jugendAlterBis <= 120;
    }

    private String herkunft;

    // Anzeigeinformationen
    private String mieterVorname;
    private String mieterName;
    private String mietobjektBezeichnung;

    private List<BuchungVerbrauchskostenDTO> verbrauchskosten;

    private List<VerbrauchskostenVorschauDTO> verbrauchskostenVorschau;

    private Rabattart rabattart = Rabattart.KEIN_RABATT;

    private RabattwertTyp rabattwertTyp;

    private java.math.BigDecimal rabattwert;

    private java.math.BigDecimal rabattbetrag = java.math.BigDecimal.ZERO;

    @jakarta.validation.constraints.DecimalMin("0.00")
    private java.math.BigDecimal kautionBetrag = java.math.BigDecimal.ZERO;

    @jakarta.validation.constraints.DecimalMin("0.00")
    private java.math.BigDecimal anzahlungBetrag = java.math.BigDecimal.ZERO;
}
