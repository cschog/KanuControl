package com.kcserver.sampleData;

import com.kcserver.entity.Verein;
import com.kcserver.repository.VereinRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Order(1)
public class SampleVereinData {
    @Bean
    public CommandLineRunner sampleVerein(VereinRepository vereinRepository) {
        return (args) -> {
            vereinRepository.save(new Verein(
                    "Eschweiler Kanu Club",
                    "EKC",
                    "Ardennenstr. 82",
                    "52076",
                    "Aachen",
                    "02408-81549",
                    "VR Bank e.G.",
                    "Eschweiler Kanu Club e.V.",
                    "Ardennenstr. 82, 52076 Aachen",
                    "DE45 3916 2980 6106 7940 20",
                    "GENODED1WUR"
            ));
            vereinRepository.save(new Verein(
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
            ));
            vereinRepository.save(new Verein(
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
            ));
            vereinRepository.save(new Verein(
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
            ));
        };
    }
}
