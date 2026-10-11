package com.kcserver.vermietung.dto;

import com.kcserver.vermietung.enumtype.Verbrauchsart;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ZaehlerDTO {

    private Long id;

    private Long mietobjektId;

    @NotBlank
    @Size(max = 100)
    private String bezeichnung;

    @NotNull
    private Verbrauchsart verbrauchsart;

    @Size(max = 100)
    private String zaehlernummer;

    @NotBlank
    @Size(max = 20)
    private String mengeneinheit;

    private boolean aktiv = true;
}