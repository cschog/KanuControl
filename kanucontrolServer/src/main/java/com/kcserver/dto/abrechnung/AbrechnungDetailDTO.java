package com.kcserver.dto.abrechnung;

import com.kcserver.dto.finanz.FinanzSummaryDTO;
import com.kcserver.enumtype.AbrechnungsStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AbrechnungDetailDTO {

    private Long id;

    private Long veranstaltungId;

    private AbrechnungsStatus status;

    private List<AbrechnungBuchungDTO> buchungen;

    private FinanzSummaryDTO finanz;

}