package com.kcserver.mapper;

import com.kcserver.dto.unterkunft.UnterkunftsartCreateUpdateDTO;
import com.kcserver.dto.unterkunft.UnterkunftsartDTO;
import com.kcserver.dto.unterkunft.UnterkunftsartRefDTO;
import com.kcserver.entity.Unterkunftsart;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UnterkunftsartMapper {

    UnterkunftsartDTO toDTO(Unterkunftsart entity);

    List<UnterkunftsartDTO> toDTO(List<Unterkunftsart> entities);

    @Mapping(target = "id", ignore = true)
    Unterkunftsart toEntity(UnterkunftsartCreateUpdateDTO dto);

    UnterkunftsartRefDTO toRef(Unterkunftsart entity);

    List<UnterkunftsartRefDTO> toRef(List<Unterkunftsart> entities);

    @Mapping(target = "id", ignore = true)
    void update(
            UnterkunftsartCreateUpdateDTO dto,
            @MappingTarget Unterkunftsart entity);
}