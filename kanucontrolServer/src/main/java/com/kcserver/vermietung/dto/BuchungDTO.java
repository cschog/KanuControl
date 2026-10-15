package com.kcserver.vermietung.dto;

import com.kcserver.vermietung.enumtype.Buchungsquelle;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BuchungDTO {

    private Long id;
    private String buchungsnummer;
    private Buchungsstatus status;

    private Instant createdAt;

    @NotNull
    private LocalDate anreise;

    private LocalDate abreise;

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

    // Anzeigeinformationen
    private String mieterVorname;
    private String mieterName;
    private String mietobjektBezeichnung;

    private List<BuchungVerbrauchskostenDTO> verbrauchskosten;

    private List<VerbrauchskostenVorschauDTO> verbrauchskostenVorschau;
}