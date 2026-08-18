package com.kcserver.mapper;

import com.kcserver.dto.mitglied.MitgliedDetailDTO;
import com.kcserver.dto.person.PersonDetailDTO;
import com.kcserver.dto.person.PersonListDTO;
import com.kcserver.dto.person.PersonRefDTO;
import com.kcserver.dto.person.PersonSaveDTO;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;

import com.kcserver.entity.Verein;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import org.springframework.lang.Nullable;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PersonMapper {

    /* LIST */
    @Mapping(
            target = "alter",
            expression = "java(calculateAlter(person.getGeburtsdatum()))"
    )
    @Mapping(
            target = "mitgliedschaftenCount",
            expression = "java(person.getMitgliedschaften() == null ? 0 : person.getMitgliedschaften().size())"
    )
    @Mapping(target = "ort", source = "ort")
    @Mapping(
            target = "hauptvereinAbk",
            expression = "java(resolveHauptvereinAbk(person))"
    )
    PersonListDTO toListDTO(Person person);

    /* DETAIL */
    @Mapping(target = "mitgliedschaften", source = "mitgliedschaften")
    PersonDetailDTO toDetailDTO(Person person);

    /* DETAIL OHNE MITGLIEDSCHAFTEN */
    @Named("toDetailDTOWithoutMitgliedschaften")
    @Mapping(target = "mitgliedschaften", ignore = true)
    PersonDetailDTO toDetailDTOWithoutMitgliedschaften(Person person);

    @Mapping(target = "personId", ignore = true)
    MitgliedDetailDTO toMitgliedDetailDTO(Mitglied mitglied);

    /* WRITE */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mitgliedschaften", ignore = true)
    @Mapping(target = "countryCode", expression = "java(mapCountryCode(dto.getCountryCode()))")
    @Mapping(target = "bic", source = "bic")
    Person toNewEntity(PersonSaveDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mitgliedschaften", ignore = true)
    @Mapping(target = "countryCode", expression = "java(mapCountryCode(dto.getCountryCode()))")
    @Mapping(target = "bic", source = "bic")
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

    @Nullable
    default String resolveHauptvereinAbk(Person person) {
        if (person == null || person.getMitgliedschaften() == null) {
            return null;
        }

        return person.getMitgliedschaften().stream()
                .filter(Mitglied::getHauptVerein)
                .map(Mitglied::getVerein)
                .filter(java.util.Objects::nonNull)
                .map(Verein::getAbk)
                .findFirst()
                .orElse(null);
    }

    default com.kcserver.enumtype.CountryCode mapCountryCode(String code) {
        if (code == null || code.isBlank()) {
            return com.kcserver.enumtype.CountryCode.DE;
        }
        return com.kcserver.enumtype.CountryCode.valueOf(code);
    }

    @Mapping(
            target = "hauptvereinAbk",
            expression = "java(resolveHauptvereinAbk(person))"
    )
    @Mapping(
            target = "verwendetInFahrtabschnitten",
            ignore = true
    )
    PersonRefDTO toPersonRefDTO(Person person);
}