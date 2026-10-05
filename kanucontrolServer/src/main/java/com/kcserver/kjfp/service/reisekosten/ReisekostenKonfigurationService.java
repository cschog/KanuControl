package com.kcserver.kjfp.service.reisekosten;

import com.kcserver.kjfp.dto.reisekosten.ReisekostenKonfigurationResponse;
import com.kcserver.kjfp.dto.reisekosten.ReisekostenKonfigurationSaveRequest;

import java.util.List;

public interface ReisekostenKonfigurationService {

    ReisekostenKonfigurationResponse getAktuell();

    Long create(
            ReisekostenKonfigurationSaveRequest request
    );

    List<ReisekostenKonfigurationResponse> list();
}
