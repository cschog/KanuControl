package com.kcserver.mapper;

import com.kcserver.dto.verpflegung.VerpflegungsmodellCreateUpdateDTO;
import com.kcserver.dto.verpflegung.VerpflegungsmodellDTO;
import com.kcserver.entity.Verpflegungsmodell;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VerpflegungsmodellMapper {

    VerpflegungsmodellDTO toDTO(Verpflegungsmodell entity);

    List<VerpflegungsmodellDTO> toDTO(List<Verpflegungsmodell> entities);

    Verpflegungsmodell toEntity(VerpflegungsmodellCreateUpdateDTO dto);

    void update(
            VerpflegungsmodellCreateUpdateDTO dto,
            @MappingTarget Verpflegungsmodell entity);
}
