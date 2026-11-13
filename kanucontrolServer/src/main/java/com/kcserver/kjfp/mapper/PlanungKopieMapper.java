package com.kcserver.kjfp.mapper;

import com.kcserver.kjfp.entity.planung.Planung;
import com.kcserver.kjfp.entity.planung.PlanungPosition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/** Neue Identitäten und Auditdaten; alle fachlichen Planungsfelder werden übernommen. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PlanungKopieMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "veranstaltung", ignore = true)
    @Mapping(target = "status", constant = "IN_BEARBEITUNG")
    @Mapping(target = "initialisiert", constant = "true")
    @Mapping(target = "positionen", source = "positionen", qualifiedByName = "positionen")
    Planung kopie(Planung quelle);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "planung", ignore = true)
    @org.mapstruct.Named("position")
    PlanungPosition kopie(PlanungPosition quelle);

    @org.mapstruct.Named("positionen")
    @org.mapstruct.IterableMapping(qualifiedByName = "position")
    java.util.List<PlanungPosition> positionen(java.util.List<PlanungPosition> quelle);
}
