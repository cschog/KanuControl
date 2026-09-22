package com.kcserver.csv;

import com.kcserver.entity.Person;
import com.kcserver.exception.ErrorMessages;
import com.kcserver.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CsvExportService {

    private final PersonRepository personRepository;

    public byte[] exportPersonen(List<Long> personIds) {

        if (personIds == null || personIds.isEmpty()) {
            throw new IllegalArgumentException(
                    ErrorMessages.CSV_NO_PERSON_SELECTED
            );
        }

        List<Person> personen = personRepository.findAllById(personIds);

        if (personen.size() != personIds.stream().distinct().count()) {
            throw new IllegalArgumentException(
                    ErrorMessages.CSV_PERSON_NOT_FOUND
            );
        }

        try {
            return CsvPersonExporter.export(personen);
        } catch (Exception e) {
            throw new IllegalStateException(
                    ErrorMessages.CSV_EXPORT_FAILED,
                    e
            );
        }
    }
}