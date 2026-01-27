package com.kcserver.mapper;

import com.kcserver.dto.*;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PersonMapper {

    /* =========================================================
       LEGACY (kann später entfernt werden)
       ========================================================= */

    @Mapping(
            target = "alter",
            expression = "java(calculateAlter(person.getGeburtsdatum()))"
    )
    PersonDTO toDTO(Person person);

    /* =========================================================
       LIST (NEU)
       ========================================================= */

    @Mapping(
            target = "mitgliedschaftenCount",
            expression = "java(person.getMitgliedschaften() != null ? person.getMitgliedschaften().size() : 0)"
    )
    PersonListDTO toListDTO(Person person);

    /* =========================================================
       DETAIL (NEU)
       ========================================================= */

    PersonDetailDTO toDetailDTO(Person person);

    MitgliedDetailDTO toMitgliedDetailDTO(Mitglied mitglied);

    VereinRefDTO toVereinRefDTO(Verein verein);

    /* =========================================================
       WRITE
       ========================================================= */

    Person toEntity(PersonSaveDTO dto);

    @Mapping(target = "mitgliedschaften", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateFromDTO(PersonSaveDTO dto, @MappingTarget Person entity);

    @AfterMapping
    default void ensureEmptyMitgliedschaften(
            Person person,
            @MappingTarget PersonDetailDTO dto
    ) {
        if (dto.getMitgliedschaften() == null) {
            dto.setMitgliedschaften(List.of());
        }
    }

    /* =========================================================
       Helper
       ========================================================= */

    default Integer calculateAlter(LocalDate geburtsdatum) {
        if (geburtsdatum == null) return null;
        return Period.between(geburtsdatum, LocalDate.now()).getYears();
    }
}