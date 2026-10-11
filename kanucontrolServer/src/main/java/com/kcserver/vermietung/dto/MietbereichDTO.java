package com.kcserver.vermietung.dto;

import com.kcserver.vermietung.enumtype.Berechnungsart;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MietbereichDTO {

    private Long id;

    private Long mietobjektId;

    private Long zaehlerId;

    @NotBlank
    @Size(max = 255)
    private String bezeichnung;

    private Integer sortierung = 0;

    @Size(max = 2000)
    private String beschreibung;

    private boolean mietbar = true;

    private boolean direktbuchungAktiv = true;

    private boolean airbnbAktiv = false;

    private boolean airbnbInserat = false;

    @NotNull
    @Min(1)
    private Integer bestand = 1;

    @Size(max = 50)
    private String mengeneinheit;

    private Berechnungsart berechnungsart = Berechnungsart.TAG;
}