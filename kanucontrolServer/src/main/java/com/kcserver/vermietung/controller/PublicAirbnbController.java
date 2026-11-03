package com.kcserver.vermietung.controller;
import com.kcserver.vermietung.ical.*;
import com.kcserver.vermietung.service.AirbnbExportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/kalender/airbnb")
public class PublicAirbnbController {
    private final AirbnbExportService service;
    @GetMapping(value="/{token}.ics", produces="text/calendar;charset=UTF-8")
    public ResponseEntity<byte[]> feed(HttpServletRequest request) {
        if (!(request.getAttribute(PublicAirbnbTenantFilter.BINDING) instanceof AirbnbExportRegistry.Config binding))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header("X-Robots-Tag","noindex, nofollow").header("Referrer-Policy","no-referrer")
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=airbnb.ics")
                .contentType(MediaType.parseMediaType("text/calendar;charset=UTF-8"))
                .body(service.render(binding).getBytes(StandardCharsets.UTF_8));
    }
}
