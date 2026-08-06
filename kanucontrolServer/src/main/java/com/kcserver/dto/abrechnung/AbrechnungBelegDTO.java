package com.kcserver.dto.abrechnung;

import com.kcserver.enumtype.BuchungsHerkunft;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AbrechnungBelegDTO {

    /* =========================================================
       BASIS
       ========================================================= */

    private Long id;

    /* =========================================================
       SYSTEM
       ========================================================= */

    private BuchungsHerkunft herkunft;

    /* =========================================================
       BELEGMETADATEN
       ========================================================= */

    private String belegnummer;

    private String externeBelegnummer;

    private String aussteller;

    private LocalDate datum;

    private String beschreibung;

    private String kuerzel;

    /* =========================================================
       DOKUMENTE
       ========================================================= */

    private List<BelegDokumentDTO> dokumente;

    /* =========================================================
       POSITIONEN
       ========================================================= */

    private List<AbrechnungBuchungDTO> positionen;
}