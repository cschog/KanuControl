package com.kcserver.vermietung.dto;

import com.kcserver.kjfp.enumtype.CountryCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MietobjektDTO {

    private Long id;

    @NotBlank
    @Size(max = 255)
    private String bezeichnung;

    private Integer sortierung = 0;

    @Size(max = 2000)
    private String beschreibung;

    @Size(max = 255)
    private String strasse;

    @Size(max = 255)
    private String plz;

    @Size(max = 255)
    private String ort;

    @jakarta.validation.Valid
    private com.kcserver.vermietung.entity.Bankverbindung bankverbindungPrimaer;

    @jakarta.validation.Valid
    private com.kcserver.vermietung.entity.Bankverbindung bankverbindungAlternativ;

    private CountryCode countryCode = CountryCode.DE;

    private LocalTime anreisezeit;
    private LocalTime abreisezeit;

    private boolean aktiv = true;

    private boolean mietbar = true;

    private boolean direktbuchungAktiv = true;

    private boolean airbnbAktiv = false;
}