package com.kcserver.vermietung.service;

import com.kcserver.core.util.PersonName;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service @Transactional(readOnly=true)
public class FinanzDashboardService {
    private final BuchungFinanzbewegungRepository bewegungen;
    private final BuchungRepository buchungen;
    private final BuchungVerbrauchskostenRepository verbrauch;
    private final FinanzDashboardJahrRepository einstellungen;
    public FinanzDashboardService(BuchungFinanzbewegungRepository bewegungen, BuchungRepository buchungen,
            BuchungVerbrauchskostenRepository verbrauch, FinanzDashboardJahrRepository einstellungen) {
        this.bewegungen=bewegungen; this.buchungen=buchungen; this.verbrauch=verbrauch; this.einstellungen=einstellungen;
    }
    public FinanzDashboardDTO get(int jahr) {
        validateYear(jahr);
        LocalDate heute=LocalDate.now(ZoneId.of("Europe/Berlin"));
        var moves=bewegungen.findForDashboard(heute);
        var bookings=buchungen.findForFinanceDashboard(LocalDate.of(jahr-1,1,1).atStartOfDay(),LocalDate.of(jahr+1,1,1).atStartOfDay());
        var ids=bookings.stream().map(Buchung::getId).toList();
        var costs=ids.isEmpty() ? List.<BuchungVerbrauchskosten>of() : verbrauch.findByBuchungIdIn(ids);
        BigDecimal grenze=einstellungen.findById(jahr).map(FinanzDashboardJahr::getUmsatzgrenze).orElse(new BigDecimal("25000.00"));
        return calculate(jahr, heute, grenze, moves, bookings, costs);
    }
    @Transactional public FinanzDashboardDTO save(int jahr, FinanzDashboardEinstellungDTO dto) {
        validateYear(jahr);
        var setting=einstellungen.findById(jahr).orElseGet(FinanzDashboardJahr::new);
        setting.setJahr(jahr); setting.setUmsatzgrenze(dto.umsatzgrenze()); einstellungen.save(setting);
        return get(jahr);
    }
    private void validateYear(int jahr) {
        if(jahr<2000 || jahr>2100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Jahr muss zwischen 2000 und 2100 liegen.");
    }
    /** Umsatz aus Zahlungsbewegungen, unabhängig von Aufenthaltsjahr und Buchungsstatus. */
    public static BigDecimal revenue(BuchungFinanzbewegung move) {
        if(move.getKategorie()==Finanzkategorie.KAUTION) return BigDecimal.ZERO;
        return move.getArt()==Finanzbewegungsart.RUECKZAHLUNG ? move.getBetrag().negate() : move.getBetrag();
    }
    public static FinanzDashboardDTO calculate(int jahr, LocalDate heute, BigDecimal grenze,
            List<BuchungFinanzbewegung> moves, List<Buchung> bookings, List<BuchungVerbrauchskosten> costs) {
        LocalDate stand=heute;
        BigDecimal[] monat=new BigDecimal[12], vorher=new BigDecimal[12]; Arrays.fill(monat,BigDecimal.ZERO); Arrays.fill(vorher,BigDecimal.ZERO);
        Map<Long,BigDecimal> bezahlt=new HashMap<>(), verbrauchBezahlt=new HashMap<>(), jahreswerte=new HashMap<>(), offen=new HashMap<>();
        Map<Long,Buchung> details=new HashMap<>(); Map<String,BigDecimal> quellen=new TreeMap<>();
        for(var move:moves) {
            if(move.getDatum().isAfter(stand)) continue;
            BigDecimal value=revenue(move); var booking=move.getBuchung();
            if (booking.getAnreise() == null) continue;
            if(move.getKategorie()==Finanzkategorie.MIETE || move.getKategorie()==Finanzkategorie.ANZAHLUNG)
                bezahlt.merge(booking.getId(),value,BigDecimal::add);
            if(move.getKategorie()==Finanzkategorie.VERBRAUCHSKOSTEN)
                verbrauchBezahlt.merge(booking.getId(),value,BigDecimal::add);
            // Bei abgeschlossenen Buchungen zählt der finale Buchungsbetrag
            // unten als Umsatz. Zahlungsvorgänge dienen hier nur zum Ermitteln
            // des offenen Saldos und dürfen nicht zusätzlich Umsatz erzeugen.
            if (booking.getStatus() == Buchungsstatus.ABGESCHLOSSEN) continue;
            int mietjahr=booking.getAnreise().getYear();
            int mietmonat=booking.getAnreise().getMonthValue()-1;
            if(mietjahr==jahr-1) vorher[mietmonat]=vorher[mietmonat].add(value);
            if(mietjahr!=jahr || value.signum()==0) continue;
            monat[mietmonat]=monat[mietmonat].add(value);
            jahreswerte.merge(booking.getId(),value,BigDecimal::add); details.put(booking.getId(),booking);
            quellen.merge(booking.getBuchungsquelle().name(),value,BigDecimal::add);
        }
        var snapshots=costs.stream().collect(Collectors.groupingBy(c->c.getBuchung().getId(),Collectors.reducing(BigDecimal.ZERO,BuchungVerbrauchskosten::getKosten,BigDecimal::add)));
        for (var booking : bookings) {
            if (booking.getStatus() != Buchungsstatus.ABGESCHLOSSEN || booking.getAnreise() == null) continue;
            int mietjahr=booking.getAnreise().getYear();
            if (mietjahr != jahr && mietjahr != jahr-1) continue;
            BigDecimal rent=booking.getMietbereiche().stream().map(BuchungPreiskalkulation::preis).reduce(BigDecimal.ZERO,BigDecimal::add)
                    .subtract(BuchungPreiskalkulation.rabatt(booking)).max(BigDecimal.ZERO);
            BigDecimal finalAmount=rent.add(Zahlungsplan.ohneAbrechnung(booking)?BigDecimal.ZERO:snapshots.getOrDefault(booking.getId(),BigDecimal.ZERO)).add(EntschaedigungPositionService.sum(booking));
            int month=booking.getAnreise().getMonthValue()-1;
            if (mietjahr == jahr-1) {
                vorher[month]=vorher[month].add(finalAmount);
            } else {
                monat[month]=monat[month].add(finalAmount);
                jahreswerte.merge(booking.getId(),finalAmount,BigDecimal::add);
                details.put(booking.getId(),booking);
                quellen.merge(booking.getBuchungsquelle().name(),finalAmount,BigDecimal::add);
            }
        }
        int unbefristet=0;
        BigDecimal forecastOpen=BigDecimal.ZERO;
        for(var booking:bookings) {
            if(booking.getAnreise()==null || booking.getAnreise().getYear()!=jahr ||
                !Set.of(Buchungsstatus.RESERVIERT,Buchungsstatus.BESTAETIGT,Buchungsstatus.ABRECHENBAR,Buchungsstatus.ABGESCHLOSSEN).contains(booking.getStatus())) continue;
            if(booking.isUnbefristet()) {unbefristet++; continue;}
            BigDecimal rent=booking.getMietbereiche().stream().map(BuchungPreiskalkulation::preis).reduce(BigDecimal.ZERO,BigDecimal::add)
                    .subtract(BuchungPreiskalkulation.rabatt(booking)).max(BigDecimal.ZERO);
            BigDecimal due=rent.subtract(bezahlt.getOrDefault(booking.getId(),BigDecimal.ZERO)).max(BigDecimal.ZERO)
                    .add((Zahlungsplan.ohneAbrechnung(booking)?BigDecimal.ZERO:snapshots.getOrDefault(booking.getId(),BigDecimal.ZERO))
                        .add(EntschaedigungPositionService.sum(booking)).subtract(verbrauchBezahlt.getOrDefault(booking.getId(),BigDecimal.ZERO)).max(BigDecimal.ZERO));
            if(due.signum()>0) {
                offen.put(booking.getId(),due);
                details.put(booking.getId(),booking);
                if (booking.getStatus() != Buchungsstatus.ABGESCHLOSSEN) forecastOpen=forecastOpen.add(due);
            }
        }
        BigDecimal sum=Arrays.stream(monat).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal prior=Arrays.stream(vorher).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal planned=offen.values().stream().reduce(BigDecimal.ZERO,BigDecimal::add);
        // Für abgeschlossene Jahre keine noch offenen Forderungen als Jahresprognose ausgeben.
        BigDecimal booked=jahr<heute.getYear() ? sum : sum.add(forecastOpen);
        BigDecimal trend=jahr==heute.getYear() ? sum.max(BigDecimal.ZERO).multiply(BigDecimal.valueOf(heute.lengthOfYear()))
                .divide(BigDecimal.valueOf(heute.getDayOfYear()),2,RoundingMode.HALF_UP) : null;
        BigDecimal forecast=trend==null ? booked : booked.max(trend);
        List<FinanzDashboardDTO.Monat> months=new ArrayList<>(); BigDecimal running=BigDecimal.ZERO,prev=BigDecimal.ZERO;
        for(int i=0;i<12;i++) {running=running.add(monat[i]);prev=prev.add(vorher[i]);months.add(new FinanzDashboardDTO.Monat(i+1,monat[i],running,prev));}
        var rows=details.values().stream().map(b->new FinanzDashboardDTO.Buchung(b.getId(),b.getBuchungsnummer(),
            PersonName.givenFirst(b.getMieter().getName(), b.getMieter().getVorname()),
            b.getMietobjekt().getBezeichnung(),b.getBuchungsquelle().name(),b.getStatus().name(),jahreswerte.getOrDefault(b.getId(),BigDecimal.ZERO),offen.getOrDefault(b.getId(),BigDecimal.ZERO)))
            .sorted(Comparator.comparing(FinanzDashboardDTO.Buchung::nummer,Comparator.nullsLast(Comparator.naturalOrder())).reversed()).toList();
        return new FinanzDashboardDTO(jahr,stand,grenze,sum,prior,grenze.subtract(sum),planned,booked,trend,forecast,unbefristet,months,rows,
            quellen.entrySet().stream().map(e->new FinanzDashboardDTO.Quelle(e.getKey(),e.getValue())).toList());
    }
}
