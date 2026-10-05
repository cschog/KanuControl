package com.kcserver.kjfp.mapper;

import com.kcserver.kjfp.dto.kik.KikZuschlagDTO;
import com.kcserver.kjfp.entity.KikZuschlag;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface KikZuschlagMapper {

    KikZuschlagDTO toDTO(KikZuschlag entity);
}