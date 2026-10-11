package com.kcserver.vermietung.dto;

import com.kcserver.vermietung.enumtype.Verbrauchsabrechnungsart;
import com.kcserver.vermietung.enumtype.Verbrauchsart;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerbrauchskostenDTO {

    private Long id;

    private Long mietobjektId;

    @NotBlank
    @Size(max = 100)
    private String bezeichnung;

    @NotNull
    private Verbrauchsart verbrauchsart;

    @NotNull
    private Verbrauchsabrechnungsart abrechnungsart;

    @Size(max = 20)
    private String mengeneinheit;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal preis;

    private boolean aktiv = true;
}