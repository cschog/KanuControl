package com.kcserver.dto.teilnehmer;

import com.kcserver.dto.zahlungsnachweis.ZahlungsnachweisListDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TeilnehmerBeitraegeResponseDTO {

    private TeilnehmerBeitragSummaryDTO summary =
            new TeilnehmerBeitragSummaryDTO();

    private List<ZahlungsnachweisListDTO> zahlungsnachweise =
            new ArrayList<>();

    private List<TeilnehmerListDTO> teilnehmer =
            new ArrayList<>();
}