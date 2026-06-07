package com.kcserver.dto.reisekosten;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReisekostenPdfRequest {

    private List<Long> abrechnungIds;
}