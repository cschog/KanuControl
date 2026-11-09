package com.kcserver.vermietung.service;

import com.kcserver.core.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.*;

/** Kein Benutzer angemeldet: verarbeitet jeden vorhandenen echten Mandanten separat. */
@Slf4j @Component @RequiredArgsConstructor
public class HausmeisterBerichtScheduler {
    private final JdbcTemplate jdbc;
    private final HausmeisterBerichtService berichte;
    private final com.kcserver.vermietung.config.HausmeisterBerichtFreigabe freigabe;

    // Wiederholung am Sonntagnachmittag ermöglicht Nachholung nach Neustart/SMTP-Ausfall.
    @Scheduled(cron = "0 */5 15-23 * * SUN", zone = "Europe/Berlin")
    public void senden() {
        if (!freigabe.isEnabled()) return;
        var day = LocalDate.now(HausmeisterBerichtService.ZONE);
        var tenants = jdbc.queryForList("""
            select distinct table_schema from information_schema.tables
            where table_name = 'homepage_anbindung' and table_schema ~ '^[a-z]{3,5}_[a-z0-9_]+$'
            order by table_schema
            """, String.class);
        String previous = TenantContext.getCurrentTenant();
        try {
            for (String tenant : tenants) {
                TenantContext.setCurrentTenant(tenant);
                try {
                    // Der Kontext ist gesetzt, bevor Spring die jeweilige Transaktion eröffnet.
                    for (Long id : berichte.objektIds()) {
                        try { berichte.wocheSenden(id, day); }
                        catch (RuntimeException ex) { log.warn("Hausmeisterbericht fehlgeschlagen: Mandant {}, Objekt {}", tenant, id, ex); }
                    }
                } catch (RuntimeException ex) { log.warn("Hausmeisterbericht: Mandant {} übersprungen", tenant, ex); }
                finally { TenantContext.clear(); }
            }
        } finally {
            if (previous == null) TenantContext.clear(); else TenantContext.setCurrentTenant(previous);
        }
    }
}
