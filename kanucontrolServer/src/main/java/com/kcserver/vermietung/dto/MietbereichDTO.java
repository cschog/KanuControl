package com.kcserver.vermietung.dto;

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

    @NotBlank
    @Size(max = 255)
    private String bezeichnung;

    @Size(max = 2000)
    private String beschreibung;

    private boolean mietbar = true;

    private boolean direktbuchungAktiv = true;

    private boolean airbnbAktiv = false;

    @NotNull
    @Min(1)
    private Integer bestand = 1;

    @Size(max = 50)
    private String mengeneinheit;
}