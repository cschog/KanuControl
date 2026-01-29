package com.kcserver.csv;

import com.kcserver.dto.PersonSaveDTO;
import com.kcserver.service.MitgliedService;
import com.kcserver.service.PersonService;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

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

    @Transactional
    public CsvImportReport importCsv(
            InputStream csv,
            InputStream mapping,
            Long vereinId,
            boolean dryRun
    ) {

        CsvImportReport report = new CsvImportReport();

        CsvMappingConfig config = CsvMappingConfig.load(mapping);
        List<CSVRecord> records = CsvReader.read(csv);

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
                }

                report.incrementCreated();

            } catch (Exception ex) {
                report.addError(rowNumber, ex.getMessage());
            }
        }

        report.setSkipped(
                report.getTotalRows()
                        - report.getCreated()
                        - report.getErrors()
        );

        return report;
    }
}