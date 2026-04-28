package com.kcserver.mapper;

import com.kcserver.dto.kik.KikZuschlagDTO;
import com.kcserver.entity.KikZuschlag;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface KikZuschlagMapper {

    KikZuschlagDTO toDTO(KikZuschlag entity);
}