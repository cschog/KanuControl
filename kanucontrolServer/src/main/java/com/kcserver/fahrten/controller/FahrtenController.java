package com.kcserver.fahrten.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.fahrten.dto.FahrtenDTOs.*;
import com.kcserver.fahrten.service.FahrtenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasAnyRole('ADMIN', 'KJFP', 'FAHRTEN')")
public class FahrtenController {
    private final FahrtenService service;
    public FahrtenController(FahrtenService service) { this.service = service; }

    @GetMapping("/kanus") public ApiResponse<List<KanuInfo>> kanus() { return ApiResponse.of(service.kanus()); }
    @PostMapping("/kanus") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<KanuInfo> createKanu(@Valid @RequestBody KanuInput input) { return ApiResponse.of(service.createKanu(input, null)); }
    @PutMapping("/kanus/{id}") public ApiResponse<KanuInfo> updateKanu(@PathVariable Long id, @Valid @RequestBody KanuInput input) { return ApiResponse.of(service.updateKanu(id, input)); }
    @GetMapping("/kanus/{id}/lizenzen") public ApiResponse<List<LizenzInfo>> lizenzen(@PathVariable Long id) { return ApiResponse.of(service.lizenzen(id)); }
    @PostMapping("/kanus/{id}/lizenzen") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<LizenzInfo> createLizenz(@PathVariable Long id, @Valid @RequestBody LizenzInput input) { return ApiResponse.of(service.createLizenz(id, input)); }

    @GetMapping("/gewaesser") public ApiResponse<List<GewaesserInfo>> gewaesser() { return ApiResponse.of(service.gewaesser()); }
    @PostMapping("/gewaesser") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<GewaesserInfo> createGewaesser(@Valid @RequestBody GewaesserInput input) { return ApiResponse.of(service.createGewaesser(input)); }

    @GetMapping("/veranstaltungen/{veranstaltungId}/kanus") public ApiResponse<List<KanuInfo>> privateKanus(@PathVariable Long veranstaltungId) { return ApiResponse.of(service.privateKanus(veranstaltungId)); }
    @PostMapping("/veranstaltungen/{veranstaltungId}/kanus") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<KanuInfo> createPrivateKanu(@PathVariable Long veranstaltungId, @Valid @RequestBody KanuInput input) { return ApiResponse.of(service.createKanu(input, veranstaltungId)); }
    @GetMapping("/veranstaltungen/{veranstaltungId}/fahrten") public ApiResponse<List<FahrtInfo>> fahrten(@PathVariable Long veranstaltungId) { return ApiResponse.of(service.fahrten(veranstaltungId)); }
    @PostMapping("/veranstaltungen/{veranstaltungId}/fahrten") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<FahrtInfo> createFahrt(@PathVariable Long veranstaltungId, @Valid @RequestBody FahrtInput input) { return ApiResponse.of(service.createFahrt(veranstaltungId, input)); }
    @PutMapping("/veranstaltungen/{veranstaltungId}/fahrten/{fahrtId}") public ApiResponse<FahrtInfo> updateFahrt(@PathVariable Long veranstaltungId, @PathVariable Long fahrtId, @Valid @RequestBody FahrtInput input) { return ApiResponse.of(service.updateFahrt(veranstaltungId, fahrtId, input)); }
    @PostMapping("/veranstaltungen/{veranstaltungId}/fahrten/{fahrtId}/kanus") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<BootInfo> addBoot(@PathVariable Long veranstaltungId, @PathVariable Long fahrtId, @Valid @RequestBody BootInput input) { return ApiResponse.of(service.addBoot(veranstaltungId, fahrtId, input)); }
    @DeleteMapping("/veranstaltungen/{veranstaltungId}/fahrten/{fahrtId}/kanus/{fahrtKanuId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void removeBoot(@PathVariable Long veranstaltungId, @PathVariable Long fahrtId, @PathVariable Long fahrtKanuId) { service.removeBoot(veranstaltungId, fahrtId, fahrtKanuId); }
    @PostMapping("/veranstaltungen/{veranstaltungId}/fahrten/{fahrtId}/kanus/{fahrtKanuId}/besatzung") public ApiResponse<FahrtInfo> addCrew(@PathVariable Long veranstaltungId, @PathVariable Long fahrtId, @PathVariable Long fahrtKanuId, @Valid @RequestBody BesatzungInput input) { return ApiResponse.of(service.addBesatzung(veranstaltungId, fahrtId, fahrtKanuId, input)); }
    @DeleteMapping("/veranstaltungen/{veranstaltungId}/fahrten/{fahrtId}/besatzung/{besatzungId}") public ApiResponse<FahrtInfo> removeCrew(@PathVariable Long veranstaltungId, @PathVariable Long fahrtId, @PathVariable Long besatzungId) { return ApiResponse.of(service.removeBesatzung(veranstaltungId, fahrtId, besatzungId)); }
    @GetMapping("/veranstaltungen/{veranstaltungId}/kilometer") public ApiResponse<List<KilometerInfo>> kilometer(@PathVariable Long veranstaltungId, @RequestParam(required = false) LocalDate von, @RequestParam(required = false) LocalDate bis) { return ApiResponse.of(service.kilometer(veranstaltungId, von, bis)); }
}
