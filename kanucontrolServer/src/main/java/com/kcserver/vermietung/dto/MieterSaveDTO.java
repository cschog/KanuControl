package com.kcserver.vermietung.dto;

import com.kcserver.core.validation.ValidIban;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MieterSaveDTO {
    private Long personId;
    @NotBlank private String name;
    @NotBlank private String vorname;
    @Email private String email;
    private String telefon;
    private String telefonFestnetz;
    private String strasse;
    private String plz;
    private String ort;
    private String countryCode = "DE";
    private String bankName;
    @ValidIban private String iban;
    private String bic;
    private Boolean aktiv;
}
