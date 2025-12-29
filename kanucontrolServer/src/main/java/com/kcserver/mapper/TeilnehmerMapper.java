package com.kcserver.mapper;

import com.kcserver.dto.TeilnehmerDTO;
import com.kcserver.entity.Teilnehmer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TeilnehmerMapper {

    @Mapping(source = "person.id", target = "personId")
    @Mapping(source = "veranstaltung.id", target = "veranstaltungId")
    TeilnehmerDTO toDTO(Teilnehmer teilnehmer);
}