package com.kcserver.service.reisekosten;

import com.kcserver.dto.reisekosten.ReisekostenKonfigurationResponse;
import com.kcserver.dto.reisekosten.ReisekostenKonfigurationSaveRequest;

import java.util.List;

public interface ReisekostenKonfigurationService {

    ReisekostenKonfigurationResponse getAktuell();

    Long create(
            ReisekostenKonfigurationSaveRequest request
    );

    List<ReisekostenKonfigurationResponse> list();
}
