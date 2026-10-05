package com.kcserver.kjfp.service.pdf;

import com.kcserver.kjfp.entity.abrechnung.AbrechnungBeleg;

import java.util.List;

public record BelegDokumentGruppe(
        int nummer,
        AbrechnungBeleg beleg,
        List<A4LayoutItem> dokumente
) {
}