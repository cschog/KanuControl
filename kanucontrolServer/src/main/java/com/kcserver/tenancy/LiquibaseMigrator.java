package com.kcserver.tenancy;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class LiquibaseMigrator {

    private final TenantLiquibaseMigrator tenantLiquibaseMigrator;
    private final GlobalLiquibaseMigrator globalLiquibaseMigrator;

    @PostConstruct
    public void migrate() {

        log.info("==================================================");
        log.info("Starting Liquibase migration");
        log.info("==================================================");

        // 1. Tenant migration first.
        //
        // Important:
        // The 'kanu' schema contains the reference data
        // required by the global migration.
        tenantLiquibaseMigrator.migrateAllSchemas();

        // 2. Global migration afterward.
        globalLiquibaseMigrator.migrateGlobal();

        log.info("==================================================");
        log.info("Liquibase migration completed");
        log.info("==================================================");
    }
}