package com.kcserver.mapper;

import com.kcserver.dto.person.PersonRefDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerDetailDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerListDTO;
import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TeilnehmerMapper {

     /* =========================
       ENTITY → LIST DTO
       ========================= */

    @Mapping(source = "person.id", target = "personId")
    @Mapping(source = "person", target = "person")
    TeilnehmerListDTO toListDTO(Teilnehmer teilnehmer);

    /* =========================
       ENTITY → DETAIL DTO
       ========================= */

    @Mapping(source = "veranstaltung.id", target = "veranstaltungId")
    @Mapping(source = "person.id", target = "personId")
    @Mapping(source = "person", target = "person")
    // rolle: null = normaler Teilnehmer
    @Mapping(source = "rolle", target = "rolle")
    TeilnehmerDetailDTO toDetailDTO(Teilnehmer teilnehmer);

    /* =========================
       HILFSMAPPING
       ========================= */

    default PersonRefDTO map(Person person) {
        if (person == null) return null;

        PersonRefDTO dto = new PersonRefDTO();
        dto.setId(person.getId());
        dto.setVorname(person.getVorname());
        dto.setName(person.getName());
        return dto;
    }
}