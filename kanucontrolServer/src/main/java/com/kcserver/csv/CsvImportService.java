package com.kcserver.csv;

import com.kcserver.dto.person.PersonSaveDTO;
import com.kcserver.service.MitgliedService;
import com.kcserver.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class CsvImportService {

    private final PersonService personService;
    private final MitgliedService mitgliedService;

    public CsvImportService(
            PersonService personService,
            MitgliedService mitgliedService
    ) {
        this.personService = personService;
        this.mitgliedService = mitgliedService;
    }

    public CsvImportReport importCsv(
            InputStream csv,
            InputStream mapping,
            Long vereinId,
            boolean dryRun
    ) {

        CsvImportReport report = new CsvImportReport();

        CsvMappingConfig config =
                mapping != null
                        ? CsvMappingConfig.load(mapping)
                        : CsvMappingConfig.defaultMapping();

        InputStream csvStream = new BufferedInputStream(csv);

// 🔑 HIER passiert alles Wichtige
        Reader reader = CsvEncoding.reader(csvStream);

// 🔑 CsvReader bekommt NUR noch Reader
        List<CSVRecord> records = CsvReader.read(reader);

        // ✅ 1. Leere CSV abfangen
        if (records.isEmpty()) {
            throw new IllegalArgumentException(
                    "Die CSV-Datei enthält keine Mitgliedsdaten. "
                            + "Bitte prüfen Sie, ob unter der Kopfzeile mindestens eine Datenzeile vorhanden ist."
            );
        }

        // ✅ 2. Header-Validierung
        Set<String> headers = records.get(0).toMap().keySet();

        if (!headers.contains("Vorname")) {
            throw new IllegalArgumentException(
                    "CSV-Header ungültig oder unerwartet – Spalte 'Vorname' fehlt"
            );
        }

        report.setTotalRows(records.size());

        for (CSVRecord record : records) {

            int rowNumber = (int) record.getRecordNumber();

            try {
                CsvPersonRow row = new CsvPersonRow(record.toMap());

                PersonSaveDTO dto =
                        CsvPersonImporter.toPersonSaveDTO(row, config);

                if (!dryRun) {
                    var created = personService.createPerson(dto);

                    mitgliedService.ensureMitglied(
                            created.getId(),
                            vereinId
                    );

                    report.incrementCreated();
                } else {
                    report.incrementSimulated();
                }

            } catch (Exception ex) {
                report.addError(rowNumber, ex.getMessage());
            }
        }

        report.setSkipped(
                report.getTotalRows()
                        - report.getCreated()
                        - report.getErrors()
        );

        log.info("CSV Import abgeschlossen: dryRun={}, created={}, errors={}",
                dryRun, report.getCreated(), report.getErrors());

        return report;
    }
}