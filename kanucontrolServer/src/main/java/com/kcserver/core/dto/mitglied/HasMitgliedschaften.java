package com.kcserver.core.dto.mitglied;

import com.kcserver.kjfp.dto.verein.HasHauptverein;

import java.util.List;

public interface HasMitgliedschaften<T extends HasHauptverein> {

    List<T> getMitgliedschaften();

}
