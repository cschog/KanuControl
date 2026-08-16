package com.kcserver.mapper;

import com.kcserver.dto.abrechnung.DokumentDTO;
import com.kcserver.entity.Dokument;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DokumentMapper {

    public DokumentDTO toDto(Dokument entity) {

        if (entity == null) {
            return null;
        }

        DokumentDTO dto = new DokumentDTO();

        dto.setId(entity.getId());
        dto.setReihenfolge(entity.getReihenfolge());
        dto.setTitel(entity.getTitel());
        dto.setOriginalDateiname(entity.getOriginalDateiname());
        dto.setMimeType(entity.getMimeType());
        dto.setDateigroesse(entity.getDateigroesse());

        dto.setBildBreitePixel(entity.getBildBreitePixel());
        dto.setBildHoehePixel(entity.getBildHoehePixel());
        dto.setDokumentBreiteMm(entity.getDokumentBreiteMm());
        dto.setDokumentHoeheMm(entity.getDokumentHoeheMm());
        dto.setReferenzObjekt(entity.getReferenzObjekt());

        dto.setCreatedAt(entity.getCreatedAt());

        return dto;
    }

    public List<DokumentDTO> toDto(List<Dokument> dokumente) {

        return dokumente.stream()
                .map(this::toDto)
                .toList();
    }
}