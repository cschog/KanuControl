package com.kcserver.mapper;

import com.kcserver.dto.person.PersonRefDTO;
import com.kcserver.dto.verein.VereinDTO;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import com.kcserver.dto.verein.VereinRefDTO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface VereinMapper {

    @Mapping(source = "kontoinhaber.id", target = "kontoinhaberId")
    @Mapping(source = "kontoinhaber", target = "kontoinhaber")
    @Mapping(expression = "java(verein.getMitglieder() != null ? verein.getMitglieder().size() : 0)", target = "mitgliederCount")
    VereinDTO toDTO(Verein verein);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "kontoinhaber", ignore = true)
    @Mapping(target = "bic", source = "bic")
    @Mapping(target = "schutzkonzept", source = "schutzkonzept")
    @Mapping(target = "mitglieder", ignore = true)
    Verein toEntity(VereinDTO dto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "abk", source = "abk")
    @Mapping(target = "ort", source = "ort")
    VereinRefDTO toRefDTO(Verein verein);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "kontoinhaber", ignore = true)
    @Mapping(target = "bic", source = "bic")
    @Mapping(target = "schutzkonzept", source = "schutzkonzept")
    @Mapping(target = "mitglieder", ignore = true)
    void updateFromDTO(VereinDTO dto, @MappingTarget Verein verein);

    default PersonRefDTO map(Person person) {
        if (person == null) return null;
        PersonRefDTO dto = new PersonRefDTO();
        dto.setId(person.getId());
        dto.setName(person.getName());
        dto.setVorname(person.getVorname());
        return dto;
    }
}
