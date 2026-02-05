package com.kcserver.mapper;

import com.kcserver.dto.mitglied.MitgliedDTO;
import com.kcserver.dto.mitglied.MitgliedDetailDTO;
import com.kcserver.dto.verein.VereinRefDTO;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Verein;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface MitgliedMapper {

    /* =====================================================
       ENTITY → DTO (flach, listenfähig, formularfähig)
       ===================================================== */

    @Mapping(source = "id", target = "id")
    @Mapping(source = "person.id", target = "personId")
    @Mapping(source = "verein.id", target = "vereinId")
    @Mapping(source = "verein.name", target = "vereinName")
    @Mapping(source = "verein.abk", target = "vereinAbk")
    @Mapping(source = "hauptVerein", target = "hauptVerein")
    MitgliedDTO toDTO(Mitglied entity);

    /* =====================================================
       ENTITY → DETAIL DTO (nur für Edit / Detail View)
       ===================================================== */

    @Mapping(source = "id", target = "id")
    @Mapping(source = "person.id", target = "personId")
    @Mapping(source = "hauptVerein", target = "hauptVerein")
    @Mapping(
            target = "verein",
            expression = "java(entity.getVerein() == null ? null : toVereinRefDTO(entity.getVerein()))"
    )
    MitgliedDetailDTO toDetailDTO(Mitglied entity);

    /* =====================================================
       DTO → ENTITY (kontrolliert, ohne Relationen)
       ===================================================== */

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "person", ignore = true)
    @Mapping(target = "verein", ignore = true)
    @Mapping(target = "hauptVerein", ignore = true)
    Mitglied toEntity(MitgliedDTO dto);

    /* =====================================================
       SUB-MAPPINGS
       ===================================================== */

    VereinRefDTO toVereinRefDTO(Verein verein);
}