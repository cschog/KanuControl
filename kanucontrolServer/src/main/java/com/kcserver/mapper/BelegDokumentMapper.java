package com.kcserver.mapper;

import com.kcserver.dto.abrechnung.BelegDokumentDTO;
import com.kcserver.entity.BelegDokument;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BelegDokumentMapper {

    public BelegDokumentDTO toDto(BelegDokument entity) {

        if (entity == null) {
            return null;
        }

        BelegDokumentDTO dto = new BelegDokumentDTO();

        dto.setId(entity.getId());
        dto.setReihenfolge(entity.getReihenfolge());
        dto.setTitel(entity.getTitel());
        dto.setOriginalDateiname(entity.getOriginalDateiname());
        dto.setMimeType(entity.getMimeType());
        dto.setDateigroesse(entity.getDateigroesse());
        dto.setCreatedAt(entity.getCreatedAt());

        return dto;
    }

    public List<BelegDokumentDTO> toDto(List<BelegDokument> dokumente) {

        return dokumente.stream()
                .map(this::toDto)
                .toList();
    }
}