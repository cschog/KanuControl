package com.kcserver.tenancy;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TenantSchemaInitializer {

    private final DataSource dataSource;
    private final Set<String> initializedTenants = ConcurrentHashMap.newKeySet();

    public TenantSchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void initializeIfNeeded(String tenant) {
        if (initializedTenants.contains(tenant)) {
            return;
        }

        synchronized (this) {
            if (initializedTenants.contains(tenant)) {
                return;
            }

            try (Connection connection = dataSource.getConnection()) {

                // 1️⃣ Schema anlegen (falls nicht vorhanden)
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("CREATE SCHEMA IF NOT EXISTS " + tenant);
                }

                // 2️⃣ Liquibase gezielt für dieses Schema ausführen
                Database database = DatabaseFactory.getInstance()
                        .findCorrectDatabaseImplementation(new JdbcConnection(connection));

                database.setDefaultSchemaName(tenant);

                Liquibase liquibase = new Liquibase(
                        "db/changelog/db.changelog-master.yaml",
                        new ClassLoaderResourceAccessor(),
                        database
                );

                liquibase.update(new Contexts(), new LabelExpression());

                initializedTenants.add(tenant);

            } catch (Exception e) {
                throw new IllegalStateException("Failed to initialize schema for tenant: " + tenant, e);
            }
        }
    }
}