package com.kcserver.vermietung.dto;

import jakarta.validation.constraints.NotBlank;
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
}