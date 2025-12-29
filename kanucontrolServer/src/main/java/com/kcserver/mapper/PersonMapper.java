package com.kcserver.mapper;

import com.kcserver.dto.PersonDTO;
import com.kcserver.entity.Person;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PersonMapper {

    PersonDTO toDTO(Person person);

    Person toEntity(PersonDTO dto);

    void updateFromDTO(PersonDTO dto, @MappingTarget Person entity);
}