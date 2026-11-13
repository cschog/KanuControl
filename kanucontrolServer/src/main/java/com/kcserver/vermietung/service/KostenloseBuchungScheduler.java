package com.kcserver.vermietung.service;

import com.kcserver.core.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Läuft auch ohne Benutzeranmeldung; jede Buchungsänderung erfolgt in der Mandantentransaktion. */
@Component @RequiredArgsConstructor @Slf4j
public class KostenloseBuchungScheduler {
    private final JdbcTemplate jdbc;
    private final BuchungService buchungen;

    @Scheduled(fixedDelay = 60000)
    public void abschliessen() {
        String previous = TenantContext.getCurrentTenant();
        try {
            var tenants = jdbc.queryForList("""
                select distinct table_schema from information_schema.tables
                where table_name = 'buchung' and table_schema ~ '^[a-z]{3,5}_[a-z0-9_]+$'
                order by table_schema
                """, String.class);
            for (String tenant : tenants) {
                TenantContext.setCurrentTenant(tenant);
                try { buchungen.aktualisiereKostenloseBuchungen(); }
                catch (RuntimeException ex) { log.warn("Kostenloser Buchungsabschluss fehlgeschlagen für Mandant {}", tenant, ex); }
                finally { TenantContext.clear(); }
            }
        } finally {
            if (previous == null) TenantContext.clear(); else TenantContext.setCurrentTenant(previous);
        }
    }
}
