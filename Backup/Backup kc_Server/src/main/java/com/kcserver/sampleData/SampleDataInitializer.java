package com.kcserver.sampleData;

import com.kcserver.sampleData.sampleService.SampleMitgliedService;
import com.kcserver.sampleData.sampleService.SamplePersonService;
import com.kcserver.sampleData.sampleService.SampleVereinService;
import com.kcserver.tenancy.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class SampleDataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(SampleDataInitializer.class);

    private final DataSource dataSource;
    private final SamplePersonService samplePersonService;
    private final SampleVereinService sampleVereinService;
    private final SampleMitgliedService sampleMitgliedService;

    // Constructor-based dependency injection
    public SampleDataInitializer(DataSource dataSource,
                                 SamplePersonService samplePersonService,
                                 SampleVereinService sampleVereinService,
                                 SampleMitgliedService sampleMitgliedService) {
        this.dataSource = dataSource;
        this.samplePersonService = samplePersonService;
        this.sampleVereinService = sampleVereinService;
        this.sampleMitgliedService = sampleMitgliedService;
    }

    @PostConstruct
    public void initializeSampleData() {
        String schemaName = null; // Define schemaName outside of the try block
        try (Connection connection = dataSource.getConnection()) {
            schemaName = TenantContext.getCurrentTenant();

            if (schemaName == null || schemaName.isBlank()) {
                logger.error("No tenant schema specified. Skipping initialization.");
                return;
            }

            // Check if schema exists
            boolean schemaExists = false;
            try (ResultSet resultSet = connection.getMetaData().getSchemas()) {
                while (resultSet.next()) {
                    if (schemaName.equalsIgnoreCase(resultSet.getString("TABLE_SCHEM"))) {
                        schemaExists = true;
                        break;
                    }
                }
            }

            if (!schemaExists) {
                logger.error("Schema '{}' does not exist. Skipping initialization.", schemaName);
                return;
            }

            logger.info("Initializing data for schema '{}'", schemaName);
            loadSampleData();
        } catch (SQLException e) {
            logger.error("Error while initializing tenant data for schema '{}': {}", schemaName, e.getMessage(), e);
        }
    }

    private void loadSampleData() {
        logger.info("Loading sample data...");
        samplePersonService.initialize();
        sampleVereinService.initialize();
        sampleMitgliedService.initialize();
        logger.info("Sample data loaded successfully.");
    }

    @Bean
    public CommandLineRunner initializeDataRunner() {
        return args -> {
            logger.info("Starting CommandLineRunner-based data initialization...");
            loadSampleData();
            logger.info("CommandLineRunner-based data initialization completed.");
        };
    }
}