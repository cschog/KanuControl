package com.kcserver.service.reisekosten;

import com.kcserver.dto.reisekosten.ReisekostenabrechnungCreateRequest;
import com.kcserver.dto.reisekosten.ReisekostenabrechnungDetailResponse;
import com.kcserver.dto.reisekosten.ReisekostenabrechnungListResponse;
import com.kcserver.dto.reisekosten.ReisekostenabrechnungUpdateRequest;

import java.util.List;

public interface ReisekostenabrechnungService {

    Long create(
            ReisekostenabrechnungCreateRequest request
    );

    ReisekostenabrechnungDetailResponse get(
            Long id
    );

    List<ReisekostenabrechnungListResponse> listByVeranstaltung(
            Long veranstaltungId
    );

    void update(
            Long id,
            ReisekostenabrechnungUpdateRequest request
    );

    void delete(
            Long id
    );
}