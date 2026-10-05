package com.kcserver.kjfp.dto.foerder;

import com.kcserver.kjfp.entity.Foerdersatz;

public record FoerdersatzLookupResult(
        Foerdersatz foerdersatz,
        boolean fallbackVerwendet
) {
}
