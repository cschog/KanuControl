package com.kcserver.sampleData;


import com.kcserver.sampleData.sampleService.SampleMitgliedService;
import com.kcserver.sampleData.sampleService.SamplePersonService;
import com.kcserver.sampleData.sampleService.SampleVereinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleDataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(SampleDataInitializer.class);

    private final SamplePersonService samplePersonService;
    private final SampleVereinService sampleVereinService;
    private final SampleMitgliedService sampleMitgliedService;

    public SampleDataInitializer(SamplePersonService samplePersonService,
                                 SampleVereinService sampleVereinService,
                                 SampleMitgliedService sampleMitgliedService) {
        this.sampleVereinService = sampleVereinService;
        this.samplePersonService = samplePersonService;
        this.sampleMitgliedService = sampleMitgliedService;
    }

    @Bean
    public CommandLineRunner initializeData() {
        return (args) -> {
            logger.info("Starting data initialization...");
            samplePersonService.initialize();
            sampleVereinService.initialize();
            sampleMitgliedService.initialize();
            logger.info("Data initialization completed.");
        };
    }
}
