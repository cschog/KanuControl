package com.kcserver.mapper;

import com.kcserver.dto.erhebungsbogen.ErhebungsbogenDTO;
import com.kcserver.entity.Erhebungsbogen;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ErhebungsbogenMapper {

    @Mapping(target = "veranstaltungId", ignore = true)
    ErhebungsbogenDTO toDTO(Erhebungsbogen entity);
}