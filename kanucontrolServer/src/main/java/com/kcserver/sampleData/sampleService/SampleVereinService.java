package com.kcserver.sampleData.sampleService;

import com.kcserver.dto.VereinDTO;
import com.kcserver.entity.Verein;
import com.kcserver.repository.VereinRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SampleVereinService {
    private static final Logger logger = LoggerFactory.getLogger(SamplePersonService.class);

    private final VereinRepository vereinRepository;

    public SampleVereinService(VereinRepository vereinRepository) {
        this.vereinRepository = vereinRepository;
    }

    public void initialize() {
        if (vereinRepository.count() == 0) { // Check if data already exists
            logger.info("Loading sample Verein data...");

            List<VereinDTO> sampleVereinDTOs = List.of(
                    new VereinDTO(
                            null, // ID will be auto-generated
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
                    new VereinDTO(
                            null,
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
                    new VereinDTO(
                            null,
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
                    new VereinDTO(
                            null,
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

            // Convert DTOs to entities and save them
            List<Verein> sampleVereine = sampleVereinDTOs.stream()
                    .map(this::convertToEntity)
                    .collect(Collectors.toList());

            vereinRepository.saveAll(sampleVereine);

            logger.info("Sample Verein data loaded successfully: {} records added.", sampleVereine.size());
        } else logger.info("Sample Verein data already exists. Skipping initialization.");
    }
    /**
     * Converts a VereinDTO to a Verein entity.
     *
     * @param vereinDTO The VereinDTO to convert.
     * @return The corresponding Verein entity.
     */
    private Verein convertToEntity(VereinDTO vereinDTO) {
        return new Verein(
                vereinDTO.getName(),
                vereinDTO.getAbk(),
                vereinDTO.getStrasse(),
                vereinDTO.getPlz(),
                vereinDTO.getOrt(),
                vereinDTO.getTelefon(),
                vereinDTO.getBankName(),
                vereinDTO.getKontoInhaber(),
                vereinDTO.getKiAnschrift(),
                vereinDTO.getIban(),
                vereinDTO.getBic()
        );
    }
}
