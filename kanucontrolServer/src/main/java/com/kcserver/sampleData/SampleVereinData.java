package com.kcserver.sampleData;

import com.kcserver.entity.Verein;
import com.kcserver.repository.VereinRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
@Order(1)
public class SampleVereinData {

    private static final Logger logger = LoggerFactory.getLogger(SampleVereinData.class);

    @Bean
    public CommandLineRunner sampleVerein(VereinRepository vereinRepository) {
        return (args) -> {
            if (vereinRepository.count() == 0) { // Check if data already exists
                logger.info("Loading sample Verein data...");

                List<Verein> sampleVereine = List.of(
                        new Verein(
                                "Eschweiler Kanu Club",
                                "EKC",
                                "Ardennenstr. 82",
                                "52076",
                                "Aachen",
                                "02408-81549",
                                "VR Bank e.G.",
                                "Eschweiler Kanu Club e.V.",
                                "Ardennenstr. 82, 52076 Aachen",
                                "DE45391629806106794020",
                                "GENODED1WUR"
                        ),
                        new Verein(
                                "Spielvereinigung Boich Thum",
                                "SVBT",
                                "",
                                "",
                                "Boich",
                                "",
                                "",
                                "Spielvereinigung Boich Thum e.V.",
                                "",
                                "",
                                ""
                        ),
                        new Verein(
                                "Oberhausener Kanu Club",
                                "OKC",
                                "",
                                "",
                                "Oberhausen",
                                "",
                                "",
                                "Oberhausener Kanu Club e.V.",
                                "",
                                "",
                                ""
                        ),
                        new Verein(
                                "Kanu Club Grün Gelb e.V.",
                                "KCG",
                                "",
                                "",
                                "Köln",
                                "",
                                "",
                                "Kanu Club Grün Gelb e.V.",
                                "",
                                "",
                                ""
                        )
                );

                vereinRepository.saveAll(sampleVereine);

                logger.info("Sample Verein data loaded successfully.");
            } else {
                logger.info("Sample Verein data already exists. Skipping initialization.");
            }
        };
    }
}