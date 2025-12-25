package com.kcserver.mapper;

import com.kcserver.dto.MitgliedDTO;
import com.kcserver.dto.PersonDTO;
import com.kcserver.dto.VereinDTO;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface EntityMapper {

    // =========================
    // Person
    // =========================

    PersonDTO toPersonDTO(Person person);

    Person toPersonEntity(PersonDTO personDTO);

    @Mapping(target = "id", ignore = true)
    void updatePersonFromDTO(PersonDTO dto, @MappingTarget Person entity);

    // =========================
    // Verein
    // =========================

    VereinDTO toVereinDTO(Verein verein);

    Verein toVereinEntity(VereinDTO vereinDTO);

    @Mapping(target = "id", ignore = true)
    void updateVereinFromDTO(VereinDTO dto, @MappingTarget Verein entity);

    // =========================
    // Mitglied
    // =========================

    @Mapping(source = "person.id", target = "personId")
    @Mapping(source = "verein.id", target = "vereinId")
    @Mapping(source = "verein.name", target = "vereinName")
    @Mapping(source = "verein.abk", target = "vereinAbk")
    MitgliedDTO toMitgliedDTO(Mitglied mitglied);

    /**
     * DTO → Entity OHNE Beziehungen!
     * Person & Verein werden im Service gesetzt.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "person", ignore = true)
    @Mapping(target = "verein", ignore = true)
    Mitglied toMitgliedEntity(MitgliedDTO mitgliedDTO);
}