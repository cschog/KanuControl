package com.kcserver.mapper;

import com.kcserver.dto.ErhebungsbogenDTO;
import com.kcserver.entity.Erhebungsbogen;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ErhebungsbogenMapper {

    ErhebungsbogenDTO toDTO(Erhebungsbogen entity);
}