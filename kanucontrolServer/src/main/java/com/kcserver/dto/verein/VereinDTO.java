package com.kcserver.dto.verein;

import com.kcserver.dto.person.PersonRefDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

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
    private String bic;
    private LocalDate schutzkonzept;

    /** WRITE */
    private Long kontoinhaberId;

    /** READ */
    private PersonRefDTO kontoinhaber;

    private long mitgliederCount;
}