package com.kcserver.dto;

import com.kcserver.entity.VeranstaltungTyp;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeranstaltungDTO {

    private Long id;

    @NotNull
    private Long vereinId;

    @NotNull
    private Long leiterId;

    @NotNull
    private VeranstaltungTyp typ;

    @NotNull
    @Size(min = 3, max = 255)
    private String name;

    @NotNull
    private LocalDate beginn;

    @NotNull
    private LocalDate ende;

    /** Plan-Zahlen für Antrag */
    @NotNull
    private Integer geplanteTeilnehmer;

    @NotNull
    private Integer geplanteMitarbeiter;

    /** Status */
    private boolean aktiv;
}