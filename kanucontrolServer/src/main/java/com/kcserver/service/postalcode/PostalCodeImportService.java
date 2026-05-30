package com.kcserver.service.postalcode;

import com.kcserver.entity.PostalCode;
import com.kcserver.enumtype.CountryCode;
import com.kcserver.enumtype.PostalCodeImportStatus;
import com.kcserver.repository.PostalCodeRepository;
import com.kcserver.dto.postalcode.PostalCodeStatusResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostalCodeImportService {

    private static final String GEONAMES_BASE_URL =
            "https://download.geonames.org/export/zip/";

    private final PostalCodeRepository repository;

    private final RestClient restClient;

    private final Map<CountryCode, PostalCodeImportStatus> importStatuses =
            new ConcurrentHashMap<>();

    @Transactional
    public void importCountry(
            CountryCode countryCode
    ) {

        importStatuses.put(
                countryCode,
                PostalCodeImportStatus.RUNNING
        );

        repository.deleteByCountryCode(countryCode);
        repository.flush();



        List<PostalCode> batch = new ArrayList<>();

        try {
            String url =
                    GEONAMES_BASE_URL +
                            countryCode.name() +
                            ".zip";

            byte[] zipBytes =
                    restClient.get()
                            .uri(url)
                            .retrieve()
                            .body(byte[].class);


            if (zipBytes == null) {
                throw new IllegalStateException("GeoNames download failed");
            }

            try (
                    ZipInputStream zis =
                            new ZipInputStream(
                                    new java.io.ByteArrayInputStream(zipBytes)
                            )
            ) {

                ZipEntry entry;

                while ((entry = zis.getNextEntry()) != null) {

                    String expectedFile =
                            countryCode.name() + ".txt";


                    if (!entry.getName().equalsIgnoreCase(expectedFile)) {
                        continue;
                    }


                    importFile(
                            countryCode,
                            zis,
                            batch
                    );

                    break;
                }
            }

            if (!batch.isEmpty()) {
                repository.saveAll(batch);
            }
            importStatuses.put(
                    countryCode,
                    PostalCodeImportStatus.SUCCESS
            );

        } catch (Exception ex) {

            importStatuses.put(
                    countryCode,
                    PostalCodeImportStatus.FAILED
            );

            log.error("Postal code import failed", ex);

            throw new RuntimeException(ex);
        }
    }

    @Transactional(readOnly = true)
    public PostalCodeStatusResponse getStatus(
            CountryCode countryCode
    ) {

        var latest =
                repository.findTopByCountryCodeOrderByImportedAtDesc(
                        countryCode
                );

        return new PostalCodeStatusResponse(
                countryCode,
                repository.countByCountryCode(countryCode),
                latest.map(PostalCode::getImportedAt).orElse(null),
                latest.map(PostalCode::getSource).orElse(null),
                importStatuses.getOrDefault(
                        countryCode,
                        PostalCodeImportStatus.IDLE
                ).name()
        );
    }

    private void importFile(
            CountryCode countryCode,
            InputStream inputStream,
            List<PostalCode> batch
    ) throws IOException {

        Set<String> seen = new HashSet<>();
        int duplicateCount = 0;

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(inputStream)
                        )
        ) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] cols = line.split("\t");

                if (cols.length < 11) {
                    continue;
                }

                String postalCodeValue = cols[1].trim();
                String city = cols[2].trim();

                String key =
                        countryCode.name()
                                + "|"
                                + postalCodeValue
                                + "|"
                                + city.toUpperCase();

                if (!seen.add(key)) {
                    duplicateCount++;
                    continue;
                }

                PostalCode postalCode =
                        PostalCode.builder()
                                .countryCode(countryCode)
                                .postalCode(postalCodeValue)
                                .city(city)
                                .state(emptyToNull(cols[3]))
                                .district(emptyToNull(cols[5]))
                                .latitude(parseDouble(cols[9]))
                                .longitude(parseDouble(cols[10]))
                                .source("GEONAMES")
                                .importedAt(LocalDateTime.now())
                                .build();

                batch.add(postalCode);

                if (batch.size() >= 1000) {

                    repository.saveAll(batch);
                    repository.flush();
                    batch.clear();
                }
            }
            log.info(
                    "Postal code import finished. Imported {} rows for {} ({} duplicates skipped)",
                    repository.countByCountryCode(countryCode),
                    countryCode,
                    duplicateCount
            );
        }
    }

    private String emptyToNull(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private Double parseDouble(String value) {

        try {
            return Double.parseDouble(value);
        } catch (Exception ex) {
            return null;
        }
    }
}