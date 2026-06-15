package com.kcserver.service.reisekosten;

import com.kcserver.dto.person.PersonRefDTO;
import com.kcserver.dto.reisekosten.ReisekostenabrechnungCreateRequest;
import com.kcserver.dto.reisekosten.ReisekostenabrechnungDetailResponse;
import com.kcserver.dto.reisekosten.ReisekostenabrechnungListResponse;
import com.kcserver.dto.reisekosten.ReisekostenabrechnungUpdateRequest;

import java.math.BigDecimal;
import java.util.List;

public interface ReisekostenabrechnungService {

    Long create(
            ReisekostenabrechnungCreateRequest request
    );

    ReisekostenabrechnungDetailResponse get(
            Long id
    );

    List<PersonRefDTO> getVerfuegbareReisekostenPersonen(
            Long veranstaltungId,
            String search
    );

    List<PersonRefDTO> getVerfuegbareMitfahrer(
            Long veranstaltungId
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
    BigDecimal getReisekostenSumme(
            Long veranstaltungId
    );
}