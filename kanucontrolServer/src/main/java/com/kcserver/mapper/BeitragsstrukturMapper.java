package com.kcserver.mapper;

import com.kcserver.dto.beitrag.BeitragsregelDTO;
import com.kcserver.dto.beitrag.BeitragsstrukturDTO;
import com.kcserver.entity.Beitragsregel;
import com.kcserver.entity.Beitragsstruktur;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface BeitragsstrukturMapper {

    BeitragsstrukturDTO toDTO(Beitragsstruktur entity);
    List<BeitragsregelDTO> mapRegeln(List<Beitragsregel> regeln);
    BeitragsregelDTO toDTO(Beitragsregel entity);
}