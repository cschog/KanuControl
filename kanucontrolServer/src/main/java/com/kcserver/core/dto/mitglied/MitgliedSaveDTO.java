package com.kcserver.core.dto.mitglied;

import com.kcserver.kjfp.dto.verein.HasHauptverein;
import com.kcserver.kjfp.enumtype.MitgliedFunktion;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MitgliedSaveDTO implements HasHauptverein {

    @NotNull
    private Long vereinId;

    private MitgliedFunktion funktion;

    private Boolean hauptVerein;

    @Override
    public Boolean getHauptVerein() {
        return hauptVerein;
    }
}