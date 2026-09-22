package com.kcserver.csv;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/csv-export")
@RequiredArgsConstructor
public class CsvExportController {

    private final CsvExportService exportService;

    @PostMapping("/personen")
    @PreAuthorize("hasAuthority('verein:write')")
    public ResponseEntity<byte[]> exportPersonen(
            @RequestBody List<Long> personIds
    ) {
        byte[] csv = exportService.exportPersonen(personIds);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=personen-export.csv"
                )
                .contentType(
                        MediaType.parseMediaType("text/csv; charset=UTF-8")
                )
                .body(csv);
    }
}