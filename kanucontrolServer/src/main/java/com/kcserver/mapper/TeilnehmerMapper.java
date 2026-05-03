package com.kcserver.mapper;

import com.kcserver.dto.person.PersonRefDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerDetailDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerKurzDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerListDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerRefDTO;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.service.TeilnehmerBeitragService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = TeilnehmerBeitragService.class
)

public abstract class TeilnehmerMapper {

    @Autowired
    protected TeilnehmerBeitragService teilnehmerBeitragService;

     /* =========================
       ENTITY → LIST DTO
       ========================= */

    @Mapping(source = "person.id", target = "personId")
    @Mapping(source = "person", target = "person")
    @Mapping(source = "rolle", target = "rolle")
    @Mapping(
            target = "effektiverBeitrag",
            expression = """
        java(
            teilnehmerBeitragService.getEffektiverBeitrag(
                teilnehmer.getVeranstaltung(),
                teilnehmer
            )
        )
    """
    )
    public abstract TeilnehmerListDTO toListDTO(
            Teilnehmer teilnehmer
    );

    /* =========================
       ENTITY → DETAIL DTO
       ========================= */

    @Mapping(source = "veranstaltung.id", target = "veranstaltungId")
    @Mapping(source = "person.id", target = "personId")
    @Mapping(source = "person", target = "person")
    // rolle: null = normaler Teilnehmer
    @Mapping(source = "rolle", target = "rolle")

    @Mapping(source = "person.geburtsdatum", target = "geburtsdatum")
    @Mapping(source = "person.plz", target = "plz")
    @Mapping(source = "person.sex", target = "sex")
    public abstract TeilnehmerDetailDTO toDetailDTO(
            Teilnehmer teilnehmer

    );

    /* =========================
   ENTITY → KURZ DTO
   ========================= */

    @Mapping(source = "id", target = "id")
    @Mapping(source = "person.id", target = "personId")
    @Mapping(source = "person.vorname", target = "vorname")
    @Mapping(source = "person.name", target = "nachname")
    public abstract TeilnehmerKurzDTO toKurzDTO(
            Teilnehmer teilnehmer

    );

     public TeilnehmerRefDTO toRefDTO(Teilnehmer t) {
        if (t == null || t.getPerson() == null) return null;

        Person p = t.getPerson();

        String hauptverein = null;
        if (p.getMitgliedschaften() != null) {
            hauptverein = p.getMitgliedschaften().stream()
                    .filter(Mitglied::getHauptVerein)
                    .map(m -> m.getVerein() != null ? m.getVerein().getAbk() : null)
                    .findFirst()
                    .orElse(null);
        }

        return new TeilnehmerRefDTO(
                p.getId(),
                p.getVorname(),
                p.getName(),
                hauptverein
        );
    }

    /* =========================
       HILFSMAPPING
       ========================= */

     public PersonRefDTO map(Person person) {
        if (person == null) return null;

        PersonRefDTO dto = new PersonRefDTO();
        dto.setId(person.getId());
        dto.setVorname(person.getVorname());
        dto.setName(person.getName());

        // ⭐ Hauptverein bestimmen
        if (person.getMitgliedschaften() != null) {
            person.getMitgliedschaften().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getHauptVerein()))
                    .findFirst()
                    .ifPresent(m -> {
                        if (m.getVerein() != null) {
                            dto.setHauptvereinAbk(m.getVerein().getAbk());
                        }
                    });
        }

        return dto;
    }
}