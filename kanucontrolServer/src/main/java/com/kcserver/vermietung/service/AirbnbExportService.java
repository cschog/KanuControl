package com.kcserver.vermietung.service;

import com.kcserver.core.tenancy.TenantContext;
import com.kcserver.vermietung.dto.AirbnbExportDTO;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.ical.*;
import com.kcserver.vermietung.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;

@Service @Transactional @RequiredArgsConstructor
public class AirbnbExportService {
    private final AirbnbExportRegistry registry;
    private final MietobjektRepository objekte;
    private final BuchungRepository buchungen;
    private String tenant() {
        String tenant = TenantContext.getCurrentTenant();
        if (tenant == null || !tenant.matches("[a-z][a-z0-9_]{0,62}")) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return tenant;
    }
    @Transactional(readOnly = true)
    public AirbnbExportDTO get(Long id) {
        Mietobjekt objekt = objekte.findById(id).orElseThrow(AirbnbExportService::notFound);
        var config = registry.get(tenant(), id);
        if (config.isPresent()) return dto(config.get(), null);
        // Bootshaus-Vorgabe: EG und OG; Extras wie Grill oder Bettwäsche nicht automatisch einschließen.
        Set<Long> ids = new HashSet<>();
        for (Mietbereich b : objekt.getMietbereiche()) if (Set.of("erdgeschoss mit wiese", "obergeschoss").contains(b.getBezeichnung().trim().toLowerCase(Locale.ROOT))) ids.add(b.getId());
        return new AirbnbExportDTO(false, ids, 0L, false, null);
    }
    public AirbnbExportDTO save(Long id, AirbnbExportDTO input, boolean neuerLink) {
        Mietobjekt objekt = objekte.findLockedById(id).orElseThrow(AirbnbExportService::notFound);
        validate(objekt, input);
        String tenant = tenant();
        var current = registry.get(tenant, id);
        if (input.version() == null || !Objects.equals(input.version(), current.map(AirbnbExportRegistry.Config::version).orElse(0L))) throw konflikt();
        String token = neuerLink ? AirbnbExportRegistry.newToken() : null;
        if (current.isEmpty()) registry.create(tenant,id,input.aktiv(),input.mietbereichIds(),token);
        else if (!registry.update(current.get(),input.aktiv(),input.mietbereichIds(),token)) throw konflikt();
        return dto(registry.get(tenant,id).orElseThrow(), token);
    }
    private void validate(Mietobjekt objekt, AirbnbExportDTO input) {
        if (!input.aktiv() && input.mietbereichIds() != null && input.mietbereichIds().size() <= 200) return;
        if (input.mietbereichIds() == null || input.mietbereichIds().isEmpty() || input.mietbereichIds().size() > 200
                || !objekt.getMietbereiche().stream().map(Mietbereich::getId).toList().containsAll(input.mietbereichIds())) {
            throw new IllegalArgumentException("Bitte die zum Airbnb-Inserat gehörenden Mietbereiche auswählen (Bootshaus: EG und OG).");
        }
        if (objekt.getAnreisezeit() == null || objekt.getAbreisezeit() == null) throw new IllegalArgumentException("Bitte die Anreise- und Abreisezeiten des Mietobjekts hinterlegen.");
    }
    @Transactional(readOnly = true)
    public String render(AirbnbExportRegistry.Config binding) {
        if (!tenant().equals(binding.tenant())) throw notFound();
        var current = registry.get(binding.tenant(), binding.objektId()).orElseThrow(AirbnbExportService::notFound);
        // Eine Rotation während eines bereits laufenden Abrufs darf nur diesen Abruf noch abschließen.
        Mietobjekt objekt = objekte.findById(binding.objektId()).orElseThrow(AirbnbExportService::notFound);
        if (current.aktiv()) {
            try { validate(objekt,dto(current,null)); }
            catch (IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Kalender vorübergehend nicht verfügbar."); }
        }
        LocalDate heute = LocalDate.now(AirbnbIcalRenderer.ZONE);
        List<Buchung> termine = current.aktiv() ? buchungen.findByMietobjektIdAndStatusInAndAnreiseLessThanAndAbreiseGreaterThan(
                objekt.getId(),List.of(Buchungsstatus.RESERVIERT,Buchungsstatus.BESTAETIGT),
                AirbnbIcalRenderer.bis(heute).plusDays(1).atStartOfDay(),AirbnbIcalRenderer.von(heute).atStartOfDay()) : List.of();
        return AirbnbIcalRenderer.render(binding.tenant(),objekt,current.ids(),termine,heute,current.aktiv());
    }
    private static AirbnbExportDTO dto(AirbnbExportRegistry.Config c, String token) {
        return new AirbnbExportDTO(c.aktiv(),c.ids(),c.version(),c.linkVorhanden(),token == null ? null : AirbnbFeedPath.PREFIX + token + ".ics");
    }
    private static ResponseStatusException konflikt() { return new ResponseStatusException(HttpStatus.CONFLICT,"Die Einstellungen wurden geändert. Bitte neu laden."); }
    private static ResponseStatusException notFound() { return new ResponseStatusException(HttpStatus.NOT_FOUND,"Kalender nicht gefunden."); }
}
