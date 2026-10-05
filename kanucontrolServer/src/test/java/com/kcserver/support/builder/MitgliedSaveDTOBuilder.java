package com.kcserver.support.builder;

import com.kcserver.core.dto.mitglied.MitgliedSaveDTO;
import com.kcserver.kjfp.enumtype.MitgliedFunktion;

public class MitgliedSaveDTOBuilder {

    private Long vereinId;
    private boolean hauptVerein = true;
    private MitgliedFunktion funktion = MitgliedFunktion.JUGENDWART;

    public static MitgliedSaveDTOBuilder aDTO() {
        return new MitgliedSaveDTOBuilder();
    }

    public MitgliedSaveDTOBuilder withVerein(Long vereinId) {
        this.vereinId = vereinId;
        return this;
    }

    public MitgliedSaveDTOBuilder notHauptverein() {
        this.hauptVerein = false;
        return this;
    }

    public MitgliedSaveDTOBuilder withFunktion(MitgliedFunktion funktion) {
        this.funktion = funktion;
        return this;
    }

    public MitgliedSaveDTO build() {
        MitgliedSaveDTO dto = new MitgliedSaveDTO();
        dto.setVereinId(vereinId);
        dto.setHauptVerein(hauptVerein);
        dto.setFunktion(funktion);
        return dto;
    }
}
