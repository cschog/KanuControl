package com.kcserver.kjfp.service.reisekosten;

import com.kcserver.core.dto.person.PersonRefDTO;
import com.kcserver.kjfp.dto.reisekosten.ReisekostenabrechnungCreateRequest;
import com.kcserver.kjfp.dto.reisekosten.ReisekostenabrechnungDetailResponse;
import com.kcserver.kjfp.dto.reisekosten.ReisekostenabrechnungListResponse;
import com.kcserver.kjfp.dto.reisekosten.ReisekostenabrechnungUpdateRequest;
import com.kcserver.kjfp.entity.fahrkosten.Reisekostenabrechnung;

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

    List<Reisekostenabrechnung> findByVeranstaltung(Long veranstaltungId);

    List<ReisekostenabrechnungListResponse> listByFinanzGruppe(
            Long veranstaltungId,
            Long finanzGruppeId
    );
}