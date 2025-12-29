package com.kcserver.mapper;

import com.kcserver.dto.VereinDTO;
import com.kcserver.entity.Verein;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface VereinMapper {

    @Mapping(source = "kontoinhaber.id", target = "kontoinhaberId")
    VereinDTO toDTO(Verein verein);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "kontoinhaber", ignore = true)
    Verein toEntity(VereinDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "kontoinhaber", ignore = true)
    void updateFromDTO(VereinDTO dto, @MappingTarget Verein verein);
}