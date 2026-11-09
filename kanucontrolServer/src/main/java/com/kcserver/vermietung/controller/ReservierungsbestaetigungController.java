package com.kcserver.vermietung.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.repository.BuchungRepository;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/vermietung/buchungen/{id}/reservierungsbestaetigung")
@PreAuthorize("hasAnyRole('ADMIN', 'VERMIETUNG')")
public class ReservierungsbestaetigungController {
    private final BuchungRepository buchungen;
    private final HomepageStatusMailService mails;
    private final ReservierungsversandService versand;
    @org.springframework.beans.factory.annotation.Autowired private BuchungService buchungService;
    public record Pruefung(String fingerprint) {}
    @PostMapping("/reservieren")
    public ResponseEntity<Void> reservieren(@PathVariable Long id, @RequestBody Pruefung pruefung) {
        buchungService.aendereStatus(id, Buchungsstatus.RESERVIERT, List.of(), true, pruefung.fingerprint());
        return ResponseEntity.noContent().build();
    }

    @GetMapping @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<HomepageStatusMailService.ReservierungVorschau>> vorschau(@PathVariable Long id) {
        // Deliberately avoid BuchungService.getById, which may update statuses while reading.
        var b = buchungen.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buchung nicht gefunden."));
        if (b.getStatus() != Buchungsstatus.ANFRAGE && b.getStatus() != Buchungsstatus.RESERVIERT)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Die Vorschau ist für Anfragen und reservierte Buchungen verfügbar.");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new ApiResponse<>(mails.reservierungVorschau(b), List.of()));
    }

    @PostMapping("/senden")
    public ResponseEntity<Void> senden(@PathVariable Long id, @RequestBody Pruefung pruefung) {
        String fehler = versand.versenden(id, null, pruefung.fingerprint());
        if (fehler != null) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, fehler);
        return ResponseEntity.noContent().build();
    }
}
