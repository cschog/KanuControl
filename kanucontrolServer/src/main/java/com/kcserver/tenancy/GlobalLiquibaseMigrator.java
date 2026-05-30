package com.kcserver.tenancy;

import jakarta.annotation.PostConstruct;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class GlobalLiquibaseMigrator {

    private final DataSource dataSource;

    @PostConstruct
    public void migrateGlobal() {

        try (Connection connection = dataSource.getConnection()) {

            connection.setSchema("public");

            Database database =
                    DatabaseFactory.getInstance()
                            .findCorrectDatabaseImplementation(
                                    new JdbcConnection(connection)
                            );

            database.setDefaultSchemaName("public");

            Liquibase liquibase =
                    new Liquibase(
                            "db/changelog/db.changelog-global.yaml",
                            new ClassLoaderResourceAccessor(),
                            database
                    );

            liquibase.update(
                    new Contexts(),
                    new LabelExpression()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Global Liquibase migration failed",
                    e
            );
        }
    }
}
