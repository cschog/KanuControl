package com.kcserver.vermietung.dto;

import com.kcserver.kjfp.enumtype.CountryCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MietobjektDTO {

    private Long id;

    private Long vereinId;

    @NotBlank
    @Size(max = 255)
    private String bezeichnung;

    @Size(max = 2000)
    private String beschreibung;

    @Size(max = 255)
    private String strasse;

    @Size(max = 255)
    private String plz;

    @Size(max = 255)
    private String ort;

    private CountryCode countryCode = CountryCode.DE;

    private boolean aktiv = true;

    private boolean mietbar = true;
}