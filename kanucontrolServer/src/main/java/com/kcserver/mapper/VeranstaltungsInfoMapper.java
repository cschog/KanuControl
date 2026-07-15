package com.kcserver.mapper;

import com.kcserver.dto.simulation.VeranstaltungsInfo;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.service.veranstaltung.VeranstaltungBerechnungsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VeranstaltungsInfoMapper {

    private final VeranstaltungBerechnungsService berechnungsService;

    public VeranstaltungsInfo toDTO(Veranstaltung v) {

        if (v == null) {
            return null;
        }

        boolean kik = v.getVerein() != null
                && v.getVerein().isKikZertifiziertAm(v.getBeginnDatum());

        return VeranstaltungsInfo.builder()
                .id(v.getId())
                .name(v.getName())
                .beginnDatum(v.getBeginnDatum())
                .endeDatum(v.getEndeDatum())
                .typ(v.getTyp())
                .tage(berechnungsService.ermittleTage(v))
                .naechte(berechnungsService.ermittleNaechte(v))
                .vereinKikZertifiziert(kik)

                .beitragsstrukturName(
                        v.getBeitragsstruktur() == null
                                ? null
                                : v.getBeitragsstruktur().getName()
                )

                .unterkunftsartName(
                        v.getUnterkunftsart() == null
                                ? null
                                : v.getUnterkunftsart().getBezeichnung()
                )

                .verpflegungsmodellName(
                        v.getVerpflegungsmodell() == null
                                ? null
                                : v.getVerpflegungsmodell().getBezeichnung()
                )

                .build();
    }
}