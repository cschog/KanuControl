package com.kcserver.sampleData;

import com.kcserver.sampleData.sampleService.SampleMitgliedService;
import com.kcserver.sampleData.sampleService.SamplePersonService;
import com.kcserver.sampleData.sampleService.SampleVereinService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

// @Component
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
        // Load sample data
        loadSampleData();
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