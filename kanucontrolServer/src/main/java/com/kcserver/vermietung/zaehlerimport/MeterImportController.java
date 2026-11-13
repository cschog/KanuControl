package com.kcserver.vermietung.zaehlerimport;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(MeterImportSecurity.PATH)
public class MeterImportController {
    private final MeterImportService service;
    public MeterImportController(MeterImportService service) { this.service = service; }
    public record Reading(@NotBlank String tenant, @NotNull @Positive Long meterId,
        @NotNull OffsetDateTime measuredAt,
        @NotNull @DecimalMin("0") @Digits(integer = 12, fraction = 3) BigDecimal value,
        @NotBlank @Pattern(regexp = "kWh") String unit,
        @NotNull @Size(max = 1000) String remark) {}
    public record Result(long id, boolean created) {}

    @PostMapping
    @PreAuthorize("hasRole('METER_IMPORT')")
    public ResponseEntity<Result> ingest(@Valid @RequestBody Reading reading) throws java.sql.SQLException {
        Result result = service.ingest(reading);
        return ResponseEntity.status(result.created() ? 201 : 200).body(result);
    }
}
