package com.kcserver.kjfp.csv;

import com.kcserver.kjfp.entity.Person;
import com.kcserver.kjfp.entity.Verein;
import com.kcserver.kjfp.service.BankLookupService;
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
    public void fillBankNameIfMissing(Person person) {

        if (person.getBankName() != null
                && !person.getBankName().isBlank()) {
            return;
        }

        if (person.getBic() == null
                || person.getBic().isBlank()) {
            return;
        }

        String bankName = findBankName(person.getBic());

        if (bankName != null) {
            person.setBankName(bankName);
        }
    }

    @Override
    public void fillBankNameIfMissing(Verein verein) {

        if (verein.getBankName() != null
                && !verein.getBankName().isBlank()) {
            return;
        }

        if (verein.getBic() == null
                || verein.getBic().isBlank()) {
            return;
        }

        String bankName = findBankName(verein.getBic());

        if (bankName != null) {
            verein.setBankName(bankName);
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