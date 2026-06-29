package com.kcserver.mapper;

import com.kcserver.dto.verpflegung.VerpflegungsmodellCreateUpdateDTO;
import com.kcserver.dto.verpflegung.VerpflegungsmodellDTO;
import com.kcserver.dto.verpflegung.VerpflegungsmodellRefDTO;
import com.kcserver.entity.Verpflegungsmodell;
import org.mapstruct.Mapper;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VerpflegungsmodellMapper {

    VerpflegungsmodellDTO toDTO(Verpflegungsmodell entity);

    List<VerpflegungsmodellDTO> toDTO(List<Verpflegungsmodell> entities);

    @Mapping(target = "id", ignore = true)
    Verpflegungsmodell toEntity(VerpflegungsmodellCreateUpdateDTO dto);

    VerpflegungsmodellRefDTO toRef(Verpflegungsmodell entity);

    List<VerpflegungsmodellRefDTO> toRef(List<Verpflegungsmodell> entity);

    @Mapping(target = "id", ignore = true)
    void update(
            VerpflegungsmodellCreateUpdateDTO dto,
            @MappingTarget Verpflegungsmodell entity);
}
