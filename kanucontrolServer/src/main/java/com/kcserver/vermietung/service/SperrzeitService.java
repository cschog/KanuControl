package com.kcserver.vermietung.service;

import com.kcserver.vermietung.dto.SperrzeitDTO;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;

@Service @Transactional @RequiredArgsConstructor
public class SperrzeitService {
    private final MietobjektRepository objekte;
    private final MietobjektSperrzeitRepository sperren;
    private final BuchungRepository buchungen;

    @Transactional(readOnly = true)
    public List<SperrzeitDTO> list(Long objektId) {
        return objekte.findById(objektId).orElseThrow(SperrzeitService::notFound)
                .getSperrzeiten().stream().map(SperrzeitService::dto).toList();
    }
    public SperrzeitDTO save(Long objektId, Long id, SperrzeitDTO input) {
        validiere(input);
        Mietobjekt objekt = objekte.findLockedById(objektId).orElseThrow(SperrzeitService::notFound);
        MietobjektSperrzeit entity = id == null ? new MietobjektSperrzeit() : objekt.getSperrzeiten().stream()
                .filter(s -> s.getId().equals(id)).findFirst().orElseThrow(SperrzeitService::notFound);
        if (id != null && !Objects.equals(entity.getVersion(), input.version())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Die Sperrzeit wurde zwischenzeitlich geändert. Bitte neu laden.");
        }
        if (objekt.getSperrzeiten().stream().anyMatch(s -> !Objects.equals(s.getId(), id)
                && ueberlappt(s.getVon(), s.getBis(), input.von(), input.bis()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Der Zeitraum überschneidet sich mit einer vorhandenen Sperrzeit.");
        }
        var konflikte = buchungen.findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(
                List.of(Buchungsstatus.RESERVIERT, Buchungsstatus.BESTAETIGT, Buchungsstatus.ABRECHENBAR, Buchungsstatus.ABGESCHLOSSEN), input.bis(), input.von())
                .stream().filter(b -> Objects.equals(b.getMietobjekt().getId(), objektId)).toList();
        if (!konflikte.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Die Sperrzeit überschneidet sich mit bestehenden Buchungen: "
                    + String.join(", ", konflikte.stream().map(Buchung::getBuchungsnummer).filter(Objects::nonNull).toList())
                    + ". Bitte zuerst die Buchungen klären.");
        }
        entity.setMietobjekt(objekt); entity.setVon(input.von()); entity.setBis(input.bis());
        entity.setGanztaegig(input.ganztaegig()); entity.setGrund(input.grund().trim());
        return dto(sperren.saveAndFlush(entity));
    }
    public void delete(Long objektId, Long id, Long version) {
        Mietobjekt objekt = objekte.findLockedById(objektId).orElseThrow(SperrzeitService::notFound);
        var entity = objekt.getSperrzeiten().stream().filter(s -> s.getId().equals(id)).findFirst().orElseThrow(SperrzeitService::notFound);
        if (!Objects.equals(entity.getVersion(), version)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Die Sperrzeit wurde zwischenzeitlich geändert. Bitte neu laden.");
        }
        objekt.getSperrzeiten().remove(entity);
        objekte.flush();
    }
    public static void pruefe(Mietobjekt objekt, LocalDateTime von, LocalDateTime bis) {
        if (objekt.getSperrzeiten().stream().anyMatch(s -> ueberlappt(s.getVon(), s.getBis(), von, bis))) {
            // Auch für Homepage-Aufrufe keine internen Sperrgründe ausgeben.
            throw new IllegalArgumentException("Das Mietobjekt ist im gewählten Zeitraum gesperrt.");
        }
    }
    public static boolean ueberlappt(LocalDateTime a, LocalDateTime b, LocalDateTime von, LocalDateTime bis) {
        return a.isBefore(bis) && b.isAfter(von);
    }
    private static void validiere(SperrzeitDTO input) {
        if (input.von() == null || input.bis() == null || !input.bis().isAfter(input.von())) {
            throw new IllegalArgumentException("Das Ende muss nach dem Beginn liegen.");
        }
        if (input.grund() == null || input.grund().isBlank() || input.grund().length() > 500) {
            throw new IllegalArgumentException("Bitte einen internen Grund mit höchstens 500 Zeichen eingeben.");
        }
        if (input.ganztaegig() && (!input.von().toLocalTime().equals(java.time.LocalTime.MIDNIGHT)
                || !input.bis().toLocalTime().equals(java.time.LocalTime.MIDNIGHT))) {
            throw new IllegalArgumentException("Ganztägige Sperren müssen um 00:00 Uhr beginnen und enden.");
        }
    }
    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Mietobjekt oder Sperrzeit nicht gefunden.");
    }
    private static SperrzeitDTO dto(MietobjektSperrzeit s) {
        return new SperrzeitDTO(s.getId(), s.getVersion(), s.getVon(), s.getBis(), s.isGanztaegig(), s.getGrund());
    }
}
