package com.kcserver.vermietung.service;

import com.kcserver.vermietung.dto.HomepageAnbindungDTO;
import com.kcserver.vermietung.entity.HomepageAnbindung;
import com.kcserver.vermietung.entity.Mietbereich;
import com.kcserver.vermietung.entity.Mietobjekt;
import com.kcserver.vermietung.repository.HomepageAnbindungRepository;
import com.kcserver.vermietung.repository.MietbereichRepository;
import com.kcserver.vermietung.repository.MietobjektRepository;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class HomepageAnbindungService {
    private final HomepageAnbindungRepository anbindungen;
    private final MietobjektRepository mietobjekte;
    private final MietbereichRepository mietbereiche;
    private final Validator validator;

    @Transactional(readOnly = true)
    public HomepageAnbindungDTO get(Long mietobjektId) {
        getMietobjekt(mietobjektId);
        return anbindungen.findById(mietobjektId).map(this::toDto)
                .orElseGet(HomepageAnbindungDTO::new);
    }

    public HomepageAnbindungDTO save(Long mietobjektId, HomepageAnbindungDTO dto) {
        // Mandant kommt ausschließlich aus dem authentifizierten TenantContext.
        Mietobjekt mietobjekt = getMietobjekt(mietobjektId);
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .sorted().collect(Collectors.joining("; ")));
        }
        String homepageUrl = httpsUrl(dto.getHomepageUrl());
        String datenschutzUrl = httpsUrl(dto.getDatenschutzUrl());
        String hausregelnUrl = httpsUrl(dto.getHausregelnUrl());
        String empfaengerEmail = trimToNull(dto.getEmpfaengerEmail());
        if (dto.isVerwaltungsBenachrichtigung() && empfaengerEmail == null) {
            throw new IllegalArgumentException("Für die Verwaltungsbenachrichtigung ist eine Empfängeradresse erforderlich.");
        }
        Set<Long> ids = new HashSet<>(dto.getErlaubteMietbereichIds());
        var bereiche = mietbereiche.findAllById(ids);
        if (bereiche.size() != ids.size() || bereiche.stream().anyMatch(b ->
                !Objects.equals(b.getMietobjekt().getId(), mietobjektId)
                        || !b.isMietbar() || !b.isDirektbuchungAktiv())) {
            throw new IllegalArgumentException("Es dürfen nur direkt buchbare Mietbereiche dieses Mietobjekts freigegeben werden.");
        }
        for (Mietbereich bereich : bereiche) {
            if (bereich.getVoraussetzungenDirekt().stream().anyMatch(v -> !ids.contains(v.getId()))) {
                throw new IllegalArgumentException("Bitte auch die erforderlichen Mietbereiche für '"
                        + bereich.getBezeichnung() + "' freigeben.");
            }
        }
        HomepageAnbindung entity = anbindungen.findById(mietobjektId).orElseGet(() -> {
            HomepageAnbindung neu = new HomepageAnbindung();
            neu.setMietobjekt(mietobjekt);
            neu.setMietobjektId(mietobjektId);
            return neu;
        });
        if (!Objects.equals(entity.getVersion(), dto.getVersion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Die Einstellungen wurden zwischenzeitlich geändert. Bitte neu laden.");
        }
        if (dto.isAktiv() && (trimToNull(dto.getTechnischerClientId()) == null
                || homepageUrl == null || datenschutzUrl == null || hausregelnUrl == null || ids.isEmpty()
                || !mietobjekt.isMietbar() || !mietobjekt.isDirektbuchungAktiv()
                || mietobjekt.getAnreisezeit() == null || mietobjekt.getAbreisezeit() == null)) {
            throw new IllegalArgumentException("Für die Freischaltung sind Client-ID, alle Links, Mietbereiche und ein direkt buchbares Mietobjekt mit Standardzeiten erforderlich.");
        }
        entity.setAktiv(dto.isAktiv());
        entity.setTechnischerClientId(trimToNull(dto.getTechnischerClientId()));
        entity.setBezeichnung(dto.getBezeichnung().trim());
        entity.setHomepageUrl(homepageUrl);
        entity.setDatenschutzUrl(datenschutzUrl);
        entity.setHausregelnUrl(hausregelnUrl);
        entity.setEmpfaengerEmail(empfaengerEmail);
        entity.setHausmeisterEmail(trimToNull(dto.getHausmeisterEmail()));
        entity.setHausmeisterCcEmail(trimToNull(dto.getHausmeisterCcEmail()));
        entity.setHausmeisterAutomatisch(dto.getHausmeisterAutomatisch());
        entity.setAntwortTestEmail(trimToNull(dto.getAntwortTestEmail()));
        entity.setAntwortCcEmail(trimToNull(dto.getAntwortCcEmail()));
        entity.setGastBestaetigung(dto.isGastBestaetigung());
        entity.setVerwaltungsBenachrichtigung(dto.isVerwaltungsBenachrichtigung());
        entity.setTeilnehmerPflicht(dto.isTeilnehmerPflicht());
        entity.getErlaubteMietbereichIds().clear();
        entity.getErlaubteMietbereichIds().addAll(ids);
        return toDto(anbindungen.saveAndFlush(entity));
    }

    private Mietobjekt getMietobjekt(Long id) {
        return mietobjekte.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Mietobjekt nicht gefunden: " + id));
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String httpsUrl(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) return null;
        try {
            URI uri = URI.create(normalized);
            if ("https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null
                    && uri.getRawUserInfo() == null) return normalized;
        } catch (IllegalArgumentException ignored) {
            // Einheitlicher Fehler ohne Wiedergabe unkontrollierter Eingaben.
        }
        throw new IllegalArgumentException("Bitte vollständige HTTPS-Adressen ohne Zugangsdaten verwenden.");
    }

    private HomepageAnbindungDTO toDto(HomepageAnbindung entity) {
        HomepageAnbindungDTO dto = new HomepageAnbindungDTO();
        dto.setVersion(entity.getVersion());
        dto.setAktiv(entity.isAktiv());
        dto.setTechnischerClientId(entity.getTechnischerClientId());
        dto.setBezeichnung(entity.getBezeichnung());
        dto.setHomepageUrl(entity.getHomepageUrl());
        dto.setDatenschutzUrl(entity.getDatenschutzUrl());
        dto.setHausregelnUrl(entity.getHausregelnUrl());
        dto.setEmpfaengerEmail(entity.getEmpfaengerEmail());
        dto.setHausmeisterEmail(entity.getHausmeisterEmail());
        dto.setHausmeisterCcEmail(entity.getHausmeisterCcEmail());
        dto.setHausmeisterAutomatisch(entity.getHausmeisterAutomatisch());
        dto.setAntwortTestEmail(entity.getAntwortTestEmail());
        dto.setAntwortCcEmail(entity.getAntwortCcEmail());
        dto.setGastBestaetigung(entity.isGastBestaetigung());
        dto.setVerwaltungsBenachrichtigung(entity.isVerwaltungsBenachrichtigung());
        dto.setTeilnehmerPflicht(entity.isTeilnehmerPflicht());
        dto.setErlaubteMietbereichIds(new HashSet<>(entity.getErlaubteMietbereichIds()));
        return dto;
    }
}
