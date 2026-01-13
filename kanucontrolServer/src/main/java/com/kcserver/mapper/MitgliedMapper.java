package com.kcserver.mapper;

import com.kcserver.dto.MitgliedDTO;
import com.kcserver.entity.Mitglied;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface MitgliedMapper {

    /* ============================
       Entity → DTO
       ============================ */

    @Mapping(source = "id", target = "id")
    @Mapping(source = "person.id", target = "personId")
    @Mapping(source = "verein.id", target = "vereinId")
    @Mapping(source = "verein.name", target = "vereinName")
    @Mapping(source = "verein.abk", target = "vereinAbk")
    @Mapping(source = "hauptVerein", target = "hauptVerein")
    MitgliedDTO toDTO(Mitglied entity);

    /* ============================
       DTO → Entity
       ============================ */

    @Mapping(target = "person", ignore = true)
    @Mapping(target = "verein", ignore = true)
    Mitglied toEntity(MitgliedDTO dto);
}