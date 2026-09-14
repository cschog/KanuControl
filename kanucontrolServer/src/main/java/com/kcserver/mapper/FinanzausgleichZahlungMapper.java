package com.kcserver.mapper;

import com.kcserver.dto.finanzen.FinanzausgleichZahlungDTO;
import com.kcserver.entity.FinanzausgleichZahlung;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FinanzausgleichZahlungMapper {

    @Mapping(
            target = "finanzGruppeId",
            source = "finanzGruppe.id"
    )
    FinanzausgleichZahlungDTO toDTO(
            FinanzausgleichZahlung entity
    );
}