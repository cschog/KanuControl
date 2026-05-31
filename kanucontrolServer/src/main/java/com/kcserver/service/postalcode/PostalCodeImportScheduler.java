package com.kcserver.service.postalcode;

import com.kcserver.entity.PostalCodeCountry;
import com.kcserver.repository.PostalCodeCountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostalCodeImportScheduler {

    private final PostalCodeCountryRepository countryRepository;
    private final PostalCodeImportService importService;

    @Scheduled(cron = "0 0 3 1 * *")
    public void runMonthlyImport() {

        List<PostalCodeCountry> countries =
                countryRepository.findByEnabledTrueAndAutoImportTrue();

        log.info(
                "Starting monthly postal code import for {} countries",
                countries.size()
        );

        for (PostalCodeCountry country : countries) {

            try {

                log.info(
                        "Importing postal codes for {}",
                        country.getCountryCode()
                );

                importService.importCountry(
                        country.getCountryCode()
                );

            } catch (Exception ex) {

                log.error(
                        "Postal code import failed for {}",
                        country.getCountryCode(),
                        ex
                );
            }
        }
    }
}
