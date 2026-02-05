package com.kcserver.dto.mitglied;

import com.kcserver.dto.verein.HasHauptverein;

import java.util.List;

public interface HasMitgliedschaften {
    List<? extends HasHauptverein> getMitgliedschaften();
}
