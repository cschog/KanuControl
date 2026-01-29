package com.kcserver.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Tag;

@SpringBootTest
@ActiveProfiles("test-liquibase")
@Tag("liquibase")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class LiquibaseBaselineIT {

    @Test
    void contextLoads_andLiquibaseRuns() {
        // kein Code nötig
    }
}