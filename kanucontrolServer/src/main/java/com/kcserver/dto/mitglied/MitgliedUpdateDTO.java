package com.kcserver.dto.mitglied;

import com.kcserver.enumtype.MitgliedFunktion;
import lombok.Data;

@Data
public class MitgliedUpdateDTO {

    private MitgliedFunktion funktion;
    private Boolean hauptVerein;
}