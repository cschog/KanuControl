package com.kcserver.vermietung.dto;

import com.kcserver.vermietung.enumtype.MietbereichEigenschaftTyp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MietbereichEigenschaftDTO {

    private Long id;

    private Long mietbereichId;

    @NotNull
    private Integer sortierung = 0;

    @NotBlank
    @Size(max = 255)
    private String bezeichnung;

    @NotNull
    private MietbereichEigenschaftTyp typ;

    @NotBlank
    @Size(max = 1000)
    private String wert;
}