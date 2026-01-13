package com.kcserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VereinDTO {

    private Long id;

    @NotNull
    @NotBlank(message = "Name darf nicht leer sein")
    @Size(min = 2, max = 100)
    private String name;

    @NotNull
    @NotBlank(message = "Abkürzung darf nicht leer sein")
    @Size(min = 1, max = 10)
    private String abk;

    private String strasse;
    private String plz;
    private String ort;
    private String telefon;

    private String bankName;
    private String iban;

    /** 👤 Person-ID des Kontoinhabers */
    private Long kontoinhaberId;
}