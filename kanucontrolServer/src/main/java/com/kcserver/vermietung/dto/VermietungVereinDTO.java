package com.kcserver.vermietung.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class VermietungVereinDTO {

    private Long id;

    @NotNull
    @NotBlank(message = "Name darf nicht leer sein")
    @Size(min = 2, max = 100)
    private String name;

    @NotNull
    @NotBlank(message = "Abkürzung darf nicht leer sein")
    @Size(min = 1, max = 10)
    private String abk;

    /**
     * Kennzeichnet den Hausverein der Vermietung.
     * Änderungen erfolgen ausschließlich über den separaten Endpunkt.
     */
    private boolean hausverein;

    private String strasse;
    private String plz;
    private String ort;

    @NotBlank
    private String countryCode;

    private String telefon;
}