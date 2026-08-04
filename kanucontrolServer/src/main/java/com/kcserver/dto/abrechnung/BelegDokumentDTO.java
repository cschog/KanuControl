package com.kcserver.dto.abrechnung;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BelegDokumentDTO {

    private Long id;

    /**
     * Reihenfolge innerhalb des Belegs.
     */
    private Integer reihenfolge;

    /**
     * Optionaler Titel (z.B. "Rechnung", "Rückseite", "Kontoauszug").
     */
    private String titel;

    /**
     * Ursprünglicher Dateiname.
     */
    private String originalDateiname;

    /**
     * MIME-Type (image/jpeg, image/png, application/pdf ...)
     */
    private String mimeType;

    /**
     * Dateigröße in Byte.
     */
    private Long dateigroesse;
}