package com.kcserver.mapper;

import com.kcserver.dto.unterkunft.UnterkunftsartCreateUpdateDTO;
import com.kcserver.dto.unterkunft.UnterkunftsartDTO;
import com.kcserver.entity.Unterkunftsart;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UnterkunftsartMapper {

    UnterkunftsartDTO toDTO(Unterkunftsart entity);

    List<UnterkunftsartDTO> toDTO(List<Unterkunftsart> entities);

    Unterkunftsart toEntity(UnterkunftsartCreateUpdateDTO dto);

    void update(
            UnterkunftsartCreateUpdateDTO dto,
            @MappingTarget Unterkunftsart entity);
}
