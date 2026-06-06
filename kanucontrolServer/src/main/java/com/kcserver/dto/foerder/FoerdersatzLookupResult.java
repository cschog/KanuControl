package com.kcserver.dto.foerder;

import com.kcserver.entity.Foerdersatz;

public record FoerdersatzLookupResult(
        Foerdersatz foerdersatz,
        boolean fallbackVerwendet
) {
}
