package com.kcserver.core.dto.mitglied;

import com.kcserver.kjfp.enumtype.MitgliedFunktion;
import lombok.Data;

@Data
public class MitgliedCreateDTO {

    private Long vereinId;
    private MitgliedFunktion funktion;
    private Boolean hauptVerein;
}
