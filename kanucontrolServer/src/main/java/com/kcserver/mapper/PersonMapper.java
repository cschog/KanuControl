package com.kcserver.mapper;

import com.kcserver.dto.mitglied.MitgliedDetailDTO;
import com.kcserver.dto.person.PersonDetailDTO;
import com.kcserver.dto.person.PersonListDTO;
import com.kcserver.dto.person.PersonSaveDTO;
import com.kcserver.dto.verein.VereinRefDTO;
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

    /* LIST */
    @Mapping(
            target = "alter",
            expression = "java(calculateAlter(person.getGeburtsdatum()))"
    )
    @Mapping(target = "mitgliedschaftenCount", ignore = true)
    @Mapping(target = "ort", source = "ort")
    @Mapping(
            target = "hauptvereinAbk",
            expression = "java(resolveHauptvereinAbk(person))"
    )
    PersonListDTO toListDTO(Person person);

    /* DETAIL */
    @Mapping(target = "mitgliedschaften", source = "mitgliedschaften")
    PersonDetailDTO toDetailDTO(Person person);

    MitgliedDetailDTO toMitgliedDetailDTO(Mitglied mitglied);
    VereinRefDTO toVereinRefDTO(Verein verein);

    /* WRITE */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mitgliedschaften", ignore = true)
    Person toNewEntity(PersonSaveDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mitgliedschaften", ignore = true)
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

    default Integer calculateAlter(LocalDate geburtsdatum) {
        if (geburtsdatum == null) return null;
        return Period.between(geburtsdatum, LocalDate.now()).getYears();
    }

    // 🔑 NEU
    default String resolveHauptvereinAbk(Person person) {
        if (person.getMitgliedschaften() == null) return null;

        return person.getMitgliedschaften().stream()
                .filter(Mitglied::getHauptVerein)
                .map(m -> m.getVerein() != null ? m.getVerein().getAbk() : null)
                .findFirst()
                .orElse(null);
    }
}