package com.kcserver.csv;

import com.kcserver.dto.person.PersonSaveDTO;
import com.kcserver.service.MitgliedService;
import com.kcserver.service.PersonService;
import com.kcserver.validation.OnCreate;
import jakarta.validation.Validator;
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
    private final Validator validator;

    public CsvImportService(
            PersonService personService,
            MitgliedService mitgliedService,
            Validator validator
    ) {
        this.personService = personService;
        this.mitgliedService = mitgliedService;
        this.validator = validator;
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

        Reader reader = CsvEncoding.reader(new BufferedInputStream(csv));
        List<CSVRecord> records = CsvReader.read(reader);

        validateHeaders(records);

        report.setTotalRows(records.size());

        for (CSVRecord record : records) {

            int rowNumber = (int) record.getRecordNumber();

            try {
                CsvPersonRow row = new CsvPersonRow(record.toMap());

                PersonSaveDTO dto =
                        CsvPersonImporter.toPersonSaveDTO(row, config);

                if (!validatePerson(dto, report, rowNumber)) {
                    continue;
                }

                importPerson(dto, vereinId, dryRun, report);

            } catch (Exception ex) {
                addImportError(report, rowNumber, ex);
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

    private void validateHeaders(List<CSVRecord> records) {
        if (records.isEmpty()) {
            throw new IllegalArgumentException(
                    "Die CSV-Datei enthält keine Mitgliedsdaten."
            );
        }

        Set<String> headers = records.getFirst().toMap().keySet();
        if (!headers.contains("Vorname")) {
            throw new IllegalArgumentException(
                    "CSV-Header ungültig – Spalte 'Vorname' fehlt"
            );
        }
    }

    private boolean validatePerson(
            PersonSaveDTO dto,
            CsvImportReport report,
            int rowNumber
    ) {
        var violations = validator.validate(dto, OnCreate.class);

        if (violations.isEmpty()) {
            return true;
        }

        for (var violation : violations) {
            report.addError(
                    rowNumber,
                    violation.getPropertyPath().toString(),
                    null,
                    violation.getMessage()
            );
        }

        return false;
    }

    private void importPerson(
            PersonSaveDTO dto,
            Long vereinId,
            boolean dryRun,
            CsvImportReport report
    ) {
        if (dryRun) {
            report.incrementSimulated();
            return;
        }

        var created = personService.createPerson(dto);
        mitgliedService.ensureMitglied(created.getId(), vereinId);
        report.incrementCreated();
    }

    private void addImportError(
            CsvImportReport report,
            int rowNumber,
            Exception ex
    ) {
        String field = null;
        String value = null;

        if (ex instanceof CsvFieldException cfe) {
            field = cfe.getField();
            value = cfe.getValue();
        }

        report.addError(
                rowNumber,
                field,
                value,
                ex.getMessage()
        );
    }
}