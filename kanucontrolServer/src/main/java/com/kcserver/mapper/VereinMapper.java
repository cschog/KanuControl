package com.kcserver.mapper;

import com.kcserver.dto.PersonRefDTO;
import com.kcserver.dto.VereinDTO;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VereinMapper {

    @Mapping(source = "kontoinhaber.id", target = "kontoinhaberId")
    @Mapping(source = "kontoinhaber", target = "kontoinhaber")
    @Mapping(expression = "java(verein.getMitglieder().size())", target = "mitgliederCount")
    VereinDTO toDTO(Verein verein);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "kontoinhaber", ignore = true)
    Verein toEntity(VereinDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "kontoinhaber", ignore = true)
    void updateFromDTO(VereinDTO dto, @MappingTarget Verein verein);

    // 🔑 Hilfsmapping für Anzeige
    default PersonRefDTO map(Person person) {
        if (person == null) return null;

        PersonRefDTO dto = new PersonRefDTO();
        dto.setId(person.getId());
        dto.setName(person.getName());
        dto.setVorname(person.getVorname());
        return dto;
    }
}