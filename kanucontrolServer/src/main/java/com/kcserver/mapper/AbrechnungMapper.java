package com.kcserver.mapper;

import com.kcserver.dto.abrechnung.*;
import com.kcserver.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AbrechnungMapper {

    /* =========================================================
       ABRECHNUNG → DTO
       ========================================================= */

    public AbrechnungDetailDTO toDTO(Abrechnung abrechnung) {

        if (abrechnung == null) return null;

        AbrechnungDetailDTO dto = new AbrechnungDetailDTO();

        dto.setVeranstaltungId(
                abrechnung.getVeranstaltung() != null
                        ? abrechnung.getVeranstaltung().getId()
                        : null
        );

        dto.setStatus(abrechnung.getStatus());

        dto.setVerwendeterFoerdersatz(
                abrechnung.getVerwendeterFoerdersatz()
        );

        dto.setBelege(
                safeList(abrechnung.getBelege())
                        .stream()
                        .map(this::toDTO)
                        .collect(Collectors.toList())
        );

        return dto;
    }

    /* =========================================================
       BELEG → DTO
       ========================================================= */

    public AbrechnungBelegDTO toDTO(AbrechnungBeleg beleg) {

        if (beleg == null) return null;

        AbrechnungBelegDTO dto = new AbrechnungBelegDTO();

        dto.setId(beleg.getId());
        dto.setBelegnummer(beleg.getBelegnummer());
        dto.setDatum(beleg.getDatum());
        dto.setBeschreibung(beleg.getBeschreibung());

        dto.setPositionen(
                safeList(beleg.getPositionen())
                        .stream()
                        .map(this::toDTO)
                        .collect(Collectors.toList())
        );

        return dto;
    }

    /* =========================================================
       POSITION → DTO
       ========================================================= */

    public AbrechnungBuchungDTO toDTO(AbrechnungBuchung pos) {

        if (pos == null) return null;

        AbrechnungBuchungDTO dto = new AbrechnungBuchungDTO();

        dto.setId(pos.getId());
        dto.setKategorie(pos.getKategorie());
        dto.setBetrag(pos.getBetrag());
        dto.setDatum(pos.getDatum());
        dto.setBeschreibung(pos.getBeschreibung());

        if (pos.getTeilnehmer() != null) {
            dto.setTeilnehmerId(pos.getTeilnehmer().getId());
        }

        // 🔥 Gruppe kommt jetzt vom Beleg
        if (pos.getBeleg() != null &&
                pos.getBeleg().getFinanzGruppe() != null) {

            dto.setKuerzel(
                    pos.getBeleg()
                            .getFinanzGruppe()
                            .getKuerzel()
            );
        }

        return dto;
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private <T> List<T> safeList(List<T> list) {
        return list != null ? list : List.of();
    }
}