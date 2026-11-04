package com.kcserver.vermietung.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name="vermietung_finanz_dashboard_jahr") @Getter @Setter @NoArgsConstructor
public class FinanzDashboardJahr {
    @Id private Integer jahr;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal umsatzgrenze;
}
