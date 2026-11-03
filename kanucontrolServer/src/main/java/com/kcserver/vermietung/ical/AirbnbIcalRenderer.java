package com.kcserver.vermietung.ical;

import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Airbnb verwendet belegte Nächte. DTEND ist das exklusive Ende des Datumsbereichs (RFC 5545). */
public final class AirbnbIcalRenderer {
    private AirbnbIcalRenderer() {}
    public static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    public static LocalDate von(LocalDate heute) { return heute.minusDays(30); }
    public static LocalDate bis(LocalDate heute) { return heute.plusYears(2).plusDays(1); }
    private record Event(String uid, LocalDate von, LocalDate bis, String titel, Instant geaendert) {}

    public static String render(String tenant, Mietobjekt objekt, Set<Long> ids, List<Buchung> buchungen, LocalDate heute, boolean aktiv) {
        List<Event> events = new ArrayList<>();
        if (aktiv) {
            Set<Long> ziel = new HashSet<>();
            for (Mietbereich b : objekt.getMietbereiche()) if (ids.contains(b.getId())) ressourcen(b, ziel, new HashSet<>());
            for (Buchung b : buchungen) {
                if (!Objects.equals(b.getMietobjekt().getId(), objekt.getId())
                        || b.getStatus() != Buchungsstatus.RESERVIERT && b.getStatus() != Buchungsstatus.BESTAETIGT) continue;
                Set<Long> belegt = new HashSet<>();
                for (BuchungMietbereich position : b.getMietbereiche()) {
                    if (position.getAnzahl() > 0) ressourcen(position.getMietbereich(), belegt, new HashSet<>());
                }
                if (Collections.disjoint(ziel, belegt)) continue;
                event(events, tenant, objekt, "buchung", b.getId(), b.getAnreise(), b.getAbreise(), "Belegt",
                        b.getLastModifiedAt() != null ? b.getLastModifiedAt() : b.getCreatedAt(), heute);
            }
            for (MietobjektSperrzeit s : objekt.getSperrzeiten()) {
                event(events, tenant, objekt, "sperre", s.getId(), s.getVon(), s.getBis(), "Gesperrt",
                        s.getLastModifiedAt() != null ? s.getLastModifiedAt() : s.getCreatedAt(), heute);
            }
        }
        events.sort(Comparator.comparing(Event::von).thenComparing(Event::uid));
        StringBuilder out = new StringBuilder("BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//KanuControl//Airbnb Export//DE\r\nCALSCALE:GREGORIAN\r\n");
        for (Event e : events) {
            out.append("BEGIN:VEVENT\r\nUID:").append(e.uid()).append("\r\n")
                    .append("DTSTAMP:").append(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC).format(e.geaendert())).append("\r\n")
                    .append("DTSTART;VALUE=DATE:").append(e.von().format(DateTimeFormatter.BASIC_ISO_DATE)).append("\r\n")
                    .append("DTEND;VALUE=DATE:").append(e.bis().format(DateTimeFormatter.BASIC_ISO_DATE)).append("\r\n")
                    .append("SUMMARY:").append(e.titel()).append("\r\nSTATUS:CONFIRMED\r\nTRANSP:OPAQUE\r\nEND:VEVENT\r\n");
        }
        return out.append("END:VCALENDAR\r\n").toString();
    }
    private static void event(List<Event> events, String tenant, Mietobjekt objekt, String typ, Long id,
            LocalDateTime start, LocalDateTime ende, String titel, Instant geaendert, LocalDate heute) {
        if (start == null || ende == null || !ende.isAfter(start)) throw new IllegalStateException("Ungültiger Belegungszeitraum.");
        LocalDate erster = start.toLocalDate();
        if (start.toLocalTime().isBefore(objekt.getAbreisezeit())) erster = erster.minusDays(1);
        LocalDate exklusiv = ende.toLocalDate();
        // Für Airbnb bleibt bei Vermietungen auch der Abreisetag vollständig gesperrt.
        // Ein Ende exakt um Mitternacht ist weiterhin die exklusive Tagesgrenze.
        boolean abreisetagSperren = "buchung".equals(typ) && !ende.toLocalTime().equals(LocalTime.MIDNIGHT);
        if (abreisetagSperren || ende.toLocalTime().isAfter(objekt.getAnreisezeit())) exklusiv = exklusiv.plusDays(1);
        if (erster.isBefore(von(heute))) erster = von(heute);
        if (exklusiv.isAfter(bis(heute))) exklusiv = bis(heute);
        if (!erster.isBefore(exklusiv)) return;
        String uid = AirbnbExportRegistry.digest(tenant + ":" + objekt.getId() + ":" + typ + ":" + id) + "@kc";
        events.add(new Event(uid, erster, exklusiv, titel, geaendert == null ? Instant.EPOCH : geaendert));
    }
    private static void ressourcen(Mietbereich b, Set<Long> ids, Set<Long> pfad) {
        if (!pfad.add(b.getId())) throw new IllegalStateException("Zyklische Mietbereichsbelegung.");
        ids.add(b.getId());
        for (Mietbereich teil : b.getBelegteMietbereiche()) ressourcen(teil, ids, pfad);
        pfad.remove(b.getId());
    }
}
