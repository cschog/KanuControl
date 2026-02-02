package com.kcserver.csv;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/csv-import")
public class CsvMappingController {

    @GetMapping("/mapping-template")
    public ResponseEntity<byte[]> downloadMappingTemplate() {

        String csv = CsvMappingTemplateGenerator.generate();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=mapping-template.csv"
                )
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv.getBytes());
    }
}