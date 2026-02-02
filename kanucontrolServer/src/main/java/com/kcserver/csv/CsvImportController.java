package com.kcserver.csv;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/csv-import")
public class CsvImportController {

    private final CsvImportService importService;

    public CsvImportController(CsvImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/verein/{vereinId}")
    @PreAuthorize("hasAuthority('verein:write')")
    public ResponseEntity<CsvImportReport> importCsv(
            @PathVariable Long vereinId,
            @RequestParam("csv") MultipartFile csv,

            // ✅ mapping optional
            @RequestParam(value = "mapping", required = false)
            MultipartFile mapping,

            @RequestParam(defaultValue = "true") boolean dryRun
    ) throws Exception {

        CsvImportReport report =
                importService.importCsv(
                        csv.getInputStream(),
                        mapping != null ? mapping.getInputStream() : null,
                        vereinId,
                        dryRun
                );

        return ResponseEntity.ok(report);
    }
}