package com.kcserver.mapper;

import com.kcserver.dto.beitrag.BeitragsregelDTO;
import com.kcserver.dto.beitrag.BeitragsstrukturDTO;
import com.kcserver.entity.Beitragsregel;
import com.kcserver.entity.Beitragsstruktur;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.Comparator;
import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public abstract class BeitragsstrukturMapper {

    /* =========================================================
       STRUKTUR
       ========================================================= */

    public BeitragsstrukturDTO toDTO(Beitragsstruktur entity) {

        if (entity == null) {
            return null;
        }

        BeitragsstrukturDTO dto = new BeitragsstrukturDTO();

        dto.setId(entity.getId());

        dto.setName(entity.getName());

        dto.setTemplate(entity.isTemplate());

        dto.setRegeln(
                mapRegeln(entity.getRegeln())
        );

        return dto;
    }

    /* =========================================================
       REGELN
       ========================================================= */

    public List<BeitragsregelDTO> mapRegeln(
            List<Beitragsregel> regeln
    ) {

        if (regeln == null) {
            return List.of();
        }

        List<Beitragsregel> sorted =
                regeln.stream()
                        .sorted(Comparator.comparing(Beitragsregel::getSortierung))
                        .toList();

        int alterVon = 0;

        List<BeitragsregelDTO> result =
                new java.util.ArrayList<>();

        for (Beitragsregel r : sorted) {

            BeitragsregelDTO dto = new BeitragsregelDTO();

            dto.setId(r.getId());

            dto.setAlterVon(alterVon);

            dto.setAlterBis(r.getAlterBis());

            dto.setRolle(r.getRolle());

            dto.setBeitrag(r.getBeitrag());

            result.add(dto);

            if (r.getAlterBis() != null) {
                alterVon = r.getAlterBis() + 1;
            }
        }

        return result;
    }
}