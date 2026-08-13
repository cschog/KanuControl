package com.kcserver.service.pdf;

import com.kcserver.entity.AbrechnungBeleg;

import java.util.List;

public record BelegDokumentGruppe(
        int nummer,
        AbrechnungBeleg beleg,
        List<A4LayoutItem> dokumente
) {
}