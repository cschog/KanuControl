package com.kcserver.mapper;

import com.kcserver.dto.zahlungsnachweis.ZahlungsnachweisDokumentDTO;
import com.kcserver.entity.ZahlungsnachweisDokument;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ZahlungsnachweisDokumentMapper {

    public ZahlungsnachweisDokumentDTO toDto(
            ZahlungsnachweisDokument entity
    ) {

        if (entity == null) {
            return null;
        }

        ZahlungsnachweisDokumentDTO dto =
                new ZahlungsnachweisDokumentDTO();

        dto.setId(entity.getId());
        dto.setReihenfolge(entity.getReihenfolge());
        dto.setTitel(entity.getTitel());
        dto.setOriginalDateiname(entity.getOriginalDateiname());
        dto.setMimeType(entity.getMimeType());
        dto.setDateigroesse(entity.getDateigroesse());
        dto.setCreatedAt(entity.getCreatedAt());

        return dto;
    }

    public List<ZahlungsnachweisDokumentDTO> toDto(
            List<ZahlungsnachweisDokument> dokumente
    ) {

        return dokumente.stream()
                .map(this::toDto)
                .toList();
    }
}