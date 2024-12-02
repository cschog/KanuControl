package com.kcserver.mapper;

import com.kcserver.dto.MitgliedDTO;
import com.kcserver.dto.PersonDTO;
import com.kcserver.dto.VereinDTO;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EntityMapper {

    // Person Mappings
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "vorname", source = "vorname")
    @Mapping(target = "strasse", source = "strasse")
    @Mapping(target = "plz", source = "plz")
    @Mapping(target = "ort", source = "ort")
    @Mapping(target = "telefon", source = "telefon")
    @Mapping(target = "bankName", source = "bankName")
    @Mapping(target = "iban", source = "iban")
    @Mapping(target = "bic", source = "bic")
    PersonDTO toPersonDTO(Person person);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "vorname", source = "vorname")
    @Mapping(target = "strasse", source = "strasse")
    @Mapping(target = "plz", source = "plz")
    @Mapping(target = "ort", source = "ort")
    @Mapping(target = "telefon", source = "telefon")
    @Mapping(target = "bankName", source = "bankName")
    @Mapping(target = "iban", source = "iban")
    @Mapping(target = "bic", source = "bic")
    Person toPersonEntity(PersonDTO personDTO);

    @Mapping(target = "id", ignore = true)
    void updatePersonFromDTO(PersonDTO personDTO, @MappingTarget Person person);

    // Verein Mappings
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "abk", source = "abk")
    @Mapping(target = "strasse", source = "strasse")
    @Mapping(target = "plz", source = "plz")
    @Mapping(target = "ort", source = "ort")
    @Mapping(target = "telefon", source = "telefon")
    @Mapping(target = "bankName", source = "bankName")
    @Mapping(target = "kontoInhaber", source = "kontoInhaber")
    @Mapping(target = "kiAnschrift", source = "kiAnschrift")
    @Mapping(target = "iban", source = "iban")
    @Mapping(target = "bic", source = "bic")
    VereinDTO toVereinDTO(Verein verein);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "abk", source = "abk")
    @Mapping(target = "strasse", source = "strasse")
    @Mapping(target = "plz", source = "plz")
    @Mapping(target = "ort", source = "ort")
    @Mapping(target = "telefon", source = "telefon")
    @Mapping(target = "bankName", source = "bankName")
    @Mapping(target = "kontoInhaber", source = "kontoInhaber")
    @Mapping(target = "kiAnschrift", source = "kiAnschrift")
    @Mapping(target = "iban", source = "iban")
    @Mapping(target = "bic", source = "bic")
    Verein toVereinEntity(VereinDTO vereinDTO);

    @Mapping(target = "id", ignore = true)
    void updateVereinFromDTO(VereinDTO vereinDTO, @MappingTarget Verein verein);

    // Mitglied Mappings
    @Mapping(source = "personMitgliedschaft.id", target = "personId")
    @Mapping(source = "vereinMitgliedschaft.id", target = "vereinId")
    @Mapping(source = "vereinMitgliedschaft.name", target = "vereinName")
    @Mapping(source = "vereinMitgliedschaft.abk", target = "vereinAbk")
    @Mapping(source = "funktion", target = "funktion")
    @Mapping(source = "hauptVerein", target = "hauptVerein")
    MitgliedDTO toMitgliedDTO(Mitglied mitglied);

    @Mapping(source = "personId", target = "personMitgliedschaft.id")
    @Mapping(source = "vereinId", target = "vereinMitgliedschaft.id")
    @Mapping(source = "funktion", target = "funktion")
    @Mapping(source = "hauptVerein", target = "hauptVerein")
    Mitglied toMitgliedEntity(MitgliedDTO mitgliedDTO);

}