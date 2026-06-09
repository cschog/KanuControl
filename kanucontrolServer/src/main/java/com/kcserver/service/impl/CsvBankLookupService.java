package com.kcserver.service.impl;

import com.kcserver.service.BankLookupService;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class CsvBankLookupService implements BankLookupService {

    private final Map<String, String> bicMap = new HashMap<>();

    @PostConstruct
    public void load() {

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        new ClassPathResource(
                                                "banks/bic-bank.csv"
                                        ).getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            reader.readLine(); // Header
            String line;
            while ((line = reader.readLine()) != null) {

                String[] parts = line.split(";", 2);

                if (parts.length == 2) {
                    bicMap.put(
                            parts[0].trim().toUpperCase(),
                            parts[1].trim()
                    );
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Konnte bic-bank.csv nicht laden",
                    e
            );
        }
    }

    @Override
    public String findBankName(String bic) {

        if (bic == null || bic.isBlank()) {
            return null;
        }

        return bicMap.get(
                bic.trim().toUpperCase()
        );
    }
}