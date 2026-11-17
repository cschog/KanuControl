package com.kcserver.vermietung.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Inklusive Datumsgrenzen; null als Enddatum bedeutet unbegrenzt. */
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Verbrauchspreis {
    @Column(name = "gueltig_von", nullable = false)
    private LocalDate gueltigVon;
    @Column(name = "gueltig_bis")
    private LocalDate gueltigBis;
    @Column(name = "preis", nullable = false, precision = 10, scale = 2)
    private BigDecimal preis;
}
