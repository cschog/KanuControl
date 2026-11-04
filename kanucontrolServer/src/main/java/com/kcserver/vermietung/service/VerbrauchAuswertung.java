package com.kcserver.vermietung.service;

import com.kcserver.vermietung.dto.VerbrauchDashboardDTO.*;
import com.kcserver.vermietung.entity.*;
import java.math.*;
import java.time.*;
import java.util.*;

/** Auswertung zwischen echten Ablesungen. Keine Extrapolation über die letzte Ablesung hinaus. */
public final class VerbrauchAuswertung {
    private static final ZoneId ZONE=ZoneId.of("Europe/Berlin");
    private static final BigDecimal ZERO=BigDecimal.ZERO;
    private VerbrauchAuswertung() {}
    private record Range(long from, long to) {}
    private static long epoch(LocalDateTime time) {return time.atZone(ZONE).toEpochSecond();}
    private static LocalDateTime local(long epoch) {return LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch),ZONE);}
    private static BigDecimal fraction(BigDecimal value,long part,long total) {
        return total<=0 ? ZERO : value.multiply(BigDecimal.valueOf(part)).divide(BigDecimal.valueOf(total),12,RoundingMode.HALF_UP);
    }
    private static BigDecimal hours(long seconds) {return BigDecimal.valueOf(seconds).divide(BigDecimal.valueOf(3600),6,RoundingMode.HALF_UP);}
    private static BigDecimal rate(BigDecimal value,long seconds) {return seconds<=0 ? null : fraction(value,86400,seconds);}
    private static List<Range> union(List<Belegung> bookings) {
        var input=bookings.stream().filter(b->b.von()!=null && b.bis()!=null && b.bis().isAfter(b.von()))
            .map(b->new Range(epoch(b.von()),epoch(b.bis()))).sorted(Comparator.comparingLong(Range::from)).toList();
        List<Range> result=new ArrayList<>();
        for(var range:input) {
            if(result.isEmpty() || result.getLast().to()<range.from()) result.add(range);
            else {var last=result.removeLast(); result.add(new Range(last.from(),Math.max(last.to(),range.to())));}
        }
        return result;
    }
    private static long booked(List<Range> ranges,long from,long to) {
        return ranges.stream().mapToLong(r->Math.max(0,Math.min(to,r.to())-Math.max(from,r.from()))).sum();
    }
    public static ZaehlerAuswertung calculate(Zaehler meter,int year,LocalDateTime now,
            List<Zaehlerstand> readings,List<Belegung> bookings,boolean shared) {
        long start=epoch(LocalDate.of(year,1,1).atStartOfDay());
        long end=Math.min(epoch(LocalDate.of(year+1,1,1).atStartOfDay()),epoch(now));
        long period=Math.max(0,end-start);
        List<Range> ranges=union(bookings);
        BigDecimal[] total=new BigDecimal[12],with=new BigDecimal[12],without=new BigDecimal[12];
        Arrays.fill(total,ZERO);Arrays.fill(with,ZERO);Arrays.fill(without,ZERO);
        long[] coveredMonth=new long[12];boolean[] estimatedMonth=new boolean[12];
        List<Intervall> intervals=new ArrayList<>();List<Punkt> points=new ArrayList<>();List<String> warnings=new ArrayList<>();
        // Doppelte identische Ablesungen sind unschädlich; widersprüchliche Zeitpunkte begrenzen keine gültige Messung.
        TreeMap<LocalDateTime,List<BigDecimal>> grouped=new TreeMap<>();
        for(var reading:readings) if(reading.getAblesedatum()!=null && !reading.getAblesedatum().isAfter(now))
            grouped.computeIfAbsent(reading.getAblesedatum(),k->new ArrayList<>()).add(reading.getZaehlerstand());
        var times=new ArrayList<>(grouped.keySet());List<BigDecimal> values=new ArrayList<>();
        int conflicts=0,invalid=0;
        for(var time:times) {
            var list=grouped.get(time);var value=list.getFirst();
            boolean bad=value==null || value.signum()<0;
            if(!bad) for(var candidate:list) if(candidate==null || candidate.compareTo(value)!=0) {bad=true;break;}
            if(bad) conflicts++;
            values.add(bad?null:value);
        }
        BigDecimal sum=ZERO,sumWith=ZERO,sumWithout=ZERO;long covered=0,coveredWith=0;
        for(int i=1;i<times.size();i++) {
            long originalStart=epoch(times.get(i-1)),originalEnd=epoch(times.get(i));
            long from=Math.max(start,originalStart),to=Math.min(end,originalEnd);
            if(to<=from || originalEnd<=originalStart) continue;
            if(values.get(i)==null || values.get(i-1)==null) continue;
            BigDecimal delta=values.get(i).subtract(values.get(i-1));
            if(delta.signum()<0) {invalid++;continue;}
            long seconds=to-from,occupied=booked(ranges,from,to);
            BigDecimal volume=fraction(delta,seconds,originalEnd-originalStart),occupiedVolume=fraction(volume,occupied,seconds);
            boolean partial=from!=originalStart || to!=originalEnd;
            boolean mixed=occupied>0 && occupied<seconds;
            var numbers=bookings.stream().filter(b->epoch(b.von())<to && epoch(b.bis())>from)
                .map(b->b.nummer()==null?"#"+b.id():b.nummer()).distinct().sorted().toList();
            intervals.add(new Intervall(local(from),local(to),times.get(i-1),times.get(i),volume,occupiedVolume,
                volume.subtract(occupiedVolume),fraction(BigDecimal.valueOf(100),occupied,seconds),partial||mixed,numbers));
            sum=sum.add(volume);sumWith=sumWith.add(occupiedVolume);sumWithout=sumWithout.add(volume.subtract(occupiedVolume));
            covered+=seconds;coveredWith+=occupied;
            // Nur vollständige Messintervalle sind unabhängige Datenpunkte, keine interpolierten Monatsstücke.
            if(!partial) points.add(new Punkt(local(from),local(to),occupied*100.0/seconds,rate(delta,seconds).doubleValue()));
            for(int month=1;month<=12;month++) {
                long monthStart=epoch(LocalDate.of(year,month,1).atStartOfDay());
                long monthEnd=epoch(LocalDate.of(year,month,1).plusMonths(1).atStartOfDay());
                long a=Math.max(from,monthStart),b=Math.min(to,monthEnd);if(b<=a) continue;
                int index=month-1;long duration=b-a,bookedSeconds=booked(ranges,a,b);
                BigDecimal v=fraction(delta,duration,originalEnd-originalStart),w=fraction(v,bookedSeconds,duration);
                total[index]=total[index].add(v);with[index]=with[index].add(w);without[index]=without[index].add(v.subtract(w));
                coveredMonth[index]+=duration;
                estimatedMonth[index]|=duration!=originalEnd-originalStart || (bookedSeconds>0 && bookedSeconds<duration);
            }
        }
        List<Monat> months=new ArrayList<>();
        for(int m=1;m<=12;m++) {
            long a=Math.max(start,epoch(LocalDate.of(year,m,1).atStartOfDay()));
            long b=Math.min(end,epoch(LocalDate.of(year,m,1).plusMonths(1).atStartOfDay()));
            months.add(new Monat(m,total[m-1],with[m-1],without[m-1],b>a?fraction(BigDecimal.valueOf(100),booked(ranges,a,b),b-a):null,
                hours(coveredMonth[m-1]),estimatedMonth[m-1]));
        }
        if(shared) warnings.add("Gemeinsamer Zähler: Die Auswertung umfasst auch Buchungen anderer zugeordneter Mietobjekte. Der Verbrauch lässt sich nicht objektweise trennen.");
        if(!meter.isAktiv()) warnings.add("Dieser Zähler ist inaktiv; vorhandene historische Messwerte werden weiterhin ausgewertet.");
        if(conflicts>0) warnings.add(conflicts+" ungültige oder widersprüchliche Ablesezeitpunkte: angrenzende Messintervalle wurden ausgelassen.");
        if(invalid>0) warnings.add(invalid+" fallende Zählerstände (z. B. Zählerwechsel): Die betroffenen Intervalle wurden ausgelassen.");
        if(covered<period) warnings.add("Der Zeitraum ist nicht vollständig durch gültige Ablesungen abgedeckt. Außerhalb gemessener Intervalle wird kein Verbrauch ergänzt.");
        if(intervals.isEmpty()) warnings.add("Keine auswertbaren Messintervalle. Es werden mindestens zwei gültige Zählerstände benötigt, die den Zeitraum berühren.");
        if(intervals.stream().anyMatch(Intervall::geschaetzt)) warnings.add("Verbrauch in gemischten oder angeschnittenen Intervallen wird zeitanteilig verteilt. Diese Zuordnung ist eine Schätzung bei angenommener gleichmäßiger Nutzung.");
        var data=new Kennzahlen(sum,sumWith,sumWithout,rate(sumWith,coveredWith),rate(sumWithout,covered-coveredWith),hours(covered),hours(period),period>0?fraction(BigDecimal.valueOf(100),covered,period):ZERO);
        var visible=bookings.stream().filter(b->epoch(b.von())<end && epoch(b.bis())>start).sorted(Comparator.comparing(Belegung::von)).toList();
        return new ZaehlerAuswertung(meter.getId(),meter.getBezeichnung(),meter.getVerbrauchsart().name(),meter.getMengeneinheit(),
            meter.isAktiv(),shared,times.isEmpty()?null:times.getLast(),data,months,intervals,visible,new Korrelation(pearson(points),points.size(),points),warnings);
    }
    private static Double pearson(List<Punkt> points) {
        if(points.size()<3) return null;
        double x=points.stream().mapToDouble(Punkt::belegungsquote).average().orElse(0),y=points.stream().mapToDouble(Punkt::pro24h).average().orElse(0);
        double xx=0,yy=0,xy=0;
        for(var p:points) {double dx=p.belegungsquote()-x,dy=p.pro24h()-y;xx+=dx*dx;yy+=dy*dy;xy+=dx*dy;}
        if(xx<=1e-12 || yy<=1e-12) return null;
        return Math.max(-1,Math.min(1,xy/Math.sqrt(xx*yy)));
    }
}
