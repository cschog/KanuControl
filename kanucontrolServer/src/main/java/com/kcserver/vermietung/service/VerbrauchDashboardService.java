package com.kcserver.vermietung.service;
import com.kcserver.vermietung.dto.VerbrauchDashboardDTO;
import com.kcserver.vermietung.dto.VerbrauchDashboardDTO.Belegung;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
@Service @Transactional(readOnly=true)
public class VerbrauchDashboardService {
    private final MietobjektRepository objekte; private final ZaehlerRepository zaehler;
    private final ZaehlerstandRepository staende; private final BuchungRepository buchungen;
    public VerbrauchDashboardService(MietobjektRepository objekte,ZaehlerRepository zaehler,ZaehlerstandRepository staende,BuchungRepository buchungen) {
        this.objekte=objekte;this.zaehler=zaehler;this.staende=staende;this.buchungen=buchungen;
    }
    public VerbrauchDashboardDTO get(Long objektId,int jahr) {
        if(jahr<2000 || jahr>2100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Jahr muss zwischen 2000 und 2100 liegen.");
        var objekt=objekte.findById(objektId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Mietobjekt nicht gefunden."));
        var now=LocalDateTime.now(ZoneId.of("Europe/Berlin"));var meters=zaehler.findForConsumptionDashboard(objektId);
        if(meters.isEmpty()) return new VerbrauchDashboardDTO(jahr,objektId,objekt.getBezeichnung(),now,List.of());
        var readings=staende.findByZaehlerIdInAndAblesedatumLessThanEqualOrderByAblesedatumAscIdAsc(meters.stream().map(Zaehler::getId).toList(),now)
            .stream().collect(Collectors.groupingBy(z->z.getZaehler().getId()));
        var objectIds=meters.stream().flatMap(m->m.getMietbereiche().stream()).map(m->m.getMietobjekt().getId()).collect(Collectors.toSet());
        var from=LocalDate.of(jahr,1,1).atStartOfDay();var until=LocalDate.of(jahr+1,1,1).atStartOfDay();if(until.isAfter(now)) until=now;
        var bookings=until.isAfter(from)?buchungen.findForConsumptionDashboard(objectIds,
            Set.of(Buchungsstatus.RESERVIERT,Buchungsstatus.BESTAETIGT,Buchungsstatus.ABRECHENBAR,Buchungsstatus.ABGESCHLOSSEN),from,until):List.<Buchung>of();
        var result=meters.stream().map(meter->{
            var areas=meter.getMietbereiche().stream().map(Mietbereich::getId).collect(Collectors.toSet());
            var matched=bookings.stream().filter(b->b.getMietbereiche().stream().anyMatch(p->areas.contains(p.getMietbereich().getId())))
                .map(b->new Belegung(b.getId(),b.getBuchungsnummer(),b.getStatus().name(),b.getAnreise(),b.getAbreise())).toList();
            boolean shared=meter.getMietbereiche().stream().anyMatch(m->!m.getMietobjekt().getId().equals(objektId));
            return VerbrauchAuswertung.calculate(meter,jahr,now,readings.getOrDefault(meter.getId(),List.of()),matched,shared);
        }).toList();
        return new VerbrauchDashboardDTO(jahr,objektId,objekt.getBezeichnung(),now,result);
    }
}
