package com.kcserver.service;

import com.kcserver.entity.Erhebungsbogen;

public interface ErhebungsbogenService {

    Erhebungsbogen getOrCreate(Long veranstaltungId);

    Erhebungsbogen berechneStatistik(Long veranstaltungId);

    Erhebungsbogen abschliessen(Long veranstaltungId);
}