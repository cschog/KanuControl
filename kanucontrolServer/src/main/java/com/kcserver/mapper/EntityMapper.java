package com.kcserver.mapper;

import com.kcserver.dto.*;
import com.kcserver.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface EntityMapper {

    //   Person
    PersonDTO toPersonDTO(Person person);
    Person toPersonEntity(PersonDTO personDTO);

    @Mapping(target = "id", ignore = true)
    void updatePersonFromDTO(PersonDTO dto, @MappingTarget Person entity);

    //  Verein
    VereinDTO toVereinDTO(Verein verein);
    Verein toVereinEntity(VereinDTO vereinDTO);

    @Mapping(target = "id", ignore = true)
    void updateVereinFromDTO(VereinDTO dto, @MappingTarget Verein entity);

    //   Mitglied
    @Mapping(source = "person.id", target = "personId")
    @Mapping(source = "verein.id", target = "vereinId")
    @Mapping(source = "verein.name", target = "vereinName")
    @Mapping(source = "verein.abk", target = "vereinAbk")
    MitgliedDTO toMitgliedDTO(Mitglied mitglied);


    //    Teilnehmer
    @Mapping(source = "person.id", target = "personId")
    @Mapping(source = "veranstaltung.id", target = "veranstaltungId")
    TeilnehmerDTO toTeilnehmerDTO(Teilnehmer teilnehmer);

    //    Veranstaltung
    VeranstaltungDTO toVeranstaltungDTO(Veranstaltung veranstaltung);
}