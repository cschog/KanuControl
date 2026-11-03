package com.kcserver.support.config;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

/** Baut auch auf einer leeren Testdatenbank die benötigten globalen Tabellen auf. */
@Configuration
@Profile("junit")
@EnableConfigurationProperties(LiquibaseProperties.class)
public class TestLiquibaseConfig {
    @Bean
    SpringLiquibase liquibase(DataSource dataSource, LiquibaseProperties properties) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(properties.getChangeLog());
        liquibase.setDefaultSchema(properties.getDefaultSchema());
        liquibase.setShouldRun(properties.isEnabled());
        return liquibase;
    }

    @Bean
    @DependsOn("liquibase")
    SpringLiquibase globalTestLiquibase(DataSource dataSource, LiquibaseProperties properties) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-junit.yaml");
        // Globale Changesets verwenden auch im Test ihre eigene Historie.
        liquibase.setDefaultSchema("public");
        liquibase.setShouldRun(properties.isEnabled());
        return liquibase;
    }
}
