package com.kcserver.dto.verein;

import com.kcserver.dto.person.PersonRefDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Data
@Schema(description = "Repräsentiert einen Verein")
public class VereinDTO {

    @Schema(description = "ID des Vereins", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "Name des Vereins", example = "Eschweiler Kanu Club")
    @NotBlank(message = "Name darf nicht leer sein")
    @Size(min = 2, max = 100)
    private String name;

    @NotNull
    @Schema(description = "Abkürzung des Vereins", example = "EKC")
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

    private LocalDate kikZertifiziertSeit;
    private LocalDate kikZertifiziertBis;

    /** WRITE */
    private Long kontoinhaberId;

    /** READ */
    private PersonRefDTO kontoinhaber;

    private long mitgliederCount;
}