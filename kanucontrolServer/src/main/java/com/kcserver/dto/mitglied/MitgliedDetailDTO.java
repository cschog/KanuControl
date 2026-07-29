package com.kcserver.dto.mitglied;

import com.kcserver.dto.verein.HasHauptverein;
import com.kcserver.dto.verein.VereinRefDTO;
import com.kcserver.enumtype.MitgliedFunktion;
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