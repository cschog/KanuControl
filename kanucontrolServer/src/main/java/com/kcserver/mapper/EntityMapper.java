package com.kcserver.mapper;

import com.kcserver.dto.MitgliedDTO;
import com.kcserver.dto.PersonDTO;
import com.kcserver.dto.VereinDTO;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VereinRepository;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = {PersonRepository.class, VereinRepository.class})
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

    @Mapping(target = "personMitgliedschaft", ignore = true) // Explicitly ignore
    @Mapping(target = "vereinMitgliedschaft", ignore = true) // Explicitly ignore
    @Mapping(target = "person", ignore = true) // Explicitly ignore
    @Mapping(target = "verein", ignore = true) // Explicitly ignore
    @Mapping(source = "funktion", target = "funktion")
    @Mapping(source = "hauptVerein", target = "hauptVerein")
    Mitglied toMitgliedEntity(MitgliedDTO mitgliedDTO);

    @AfterMapping
    default void mapMitgliedAssociations(MitgliedDTO mitgliedDTO, @MappingTarget Mitglied mitglied,
                                         @Context PersonRepository personRepository,
                                         @Context VereinRepository vereinRepository) {
        if (mitgliedDTO.getPersonId() != null) {
            Person person = personRepository.findById(mitgliedDTO.getPersonId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid person ID"));
            mitglied.setPersonMitgliedschaft(person);
        }
        if (mitgliedDTO.getVereinId() != null) {
            Verein verein = vereinRepository.findById(mitgliedDTO.getVereinId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid verein ID"));
            mitglied.setVereinMitgliedschaft(verein);
        }
    }
}