package com.kcserver.dto;

import com.kcserver.enumtype.MitgliedFunktion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MitgliedDetailDTO {

    private Long id;
    private boolean hauptVerein;
    private MitgliedFunktion funktion;
    private VereinRefDTO verein;

}