package com.kcserver.core.dto.mitglied;

import com.kcserver.kjfp.dto.verein.HasHauptverein;
import com.kcserver.kjfp.dto.verein.VereinRefDTO;
import com.kcserver.kjfp.enumtype.MitgliedFunktion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MitgliedDetailDTO implements HasHauptverein {

    private Long id;

    private Long personId;

    private MitgliedFunktion funktion;

    private Boolean hauptVerein;

    @Override
    public Boolean getHauptVerein() {
        return hauptVerein;
    }

    private VereinRefDTO verein;
}