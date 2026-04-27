package com.kcserver.service;

import com.kcserver.dto.foerder.FoerdersatzCreateUpdateDTO;
import com.kcserver.dto.foerder.FoerdersatzDTO;
import com.kcserver.entity.Foerdersatz;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.mapper.FoerdersatzMapper;
import com.kcserver.repository.FoerdersatzRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FoerdersatzService {

    private final FoerdersatzRepository repository;
    private final FoerdersatzMapper mapper;

    /* ========================================================= */

    public FoerdersatzDTO create(FoerdersatzCreateUpdateDTO dto) {

        validateNoOverlap(
                null,
                dto.getTyp(),
                dto.getGueltigVon(),
                dto.getGueltigBis()
        );

        Foerdersatz entity = new Foerdersatz();

        mapper.applyToEntity(dto, entity);

        return mapper.toDTO(
                repository.save(entity)
        );
    }

    /* ========================================================= */

    public FoerdersatzDTO update(
            Long id,
            FoerdersatzCreateUpdateDTO dto
    ) {

        Foerdersatz entity = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Fördersatz nicht gefunden"
                        ));

        validateNoOverlap(
                id,
                dto.getTyp(),
                dto.getGueltigVon(),
                dto.getGueltigBis()
        );

        mapper.applyToEntity(dto, entity);

        return mapper.toDTO(
                repository.save(entity)
        );
    }

    /* ========================================================= */

    private void validateNoOverlap(
            Long ignoreId,
            VeranstaltungTyp typ,
            LocalDate von,
            LocalDate bis
    ) {

        if (von == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "gueltigVon darf nicht null sein"
            );
        }

        List<Foerdersatz> overlaps;

        if (bis == null) {

            overlaps = repository.findOverlappingOpenEnded(
                    typ,
                    ignoreId
            );

        } else {

            overlaps = repository.findOverlapping(
                    typ,
                    von,
                    bis,
                    ignoreId
            );
        }

        if (!overlaps.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Zeitraum überschneidet sich mit bestehendem Fördersatz"
            );
        }
    }

    /* ========================================================= */

    @Transactional(readOnly = true)
    public Foerdersatz findEntityGueltigFuerTypAm(
            VeranstaltungTyp typ,
            LocalDate datum
    ) {
        return repository.findGueltigFuerTypAm(typ, datum)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Kein gültiger Fördersatz für Typ gefunden"
                        ));
    }

    @Transactional(readOnly = true)
    public FoerdersatzDTO getById(Long id) {

        Foerdersatz entity = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Fördersatz nicht gefunden"
                        ));

        return mapper.toDTO(entity);
    }
    @Transactional(readOnly = true)
    public List<FoerdersatzDTO> getAll() {

        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    public void delete(Long id) {

        if (!repository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Fördersatz nicht gefunden"
            );
        }

        repository.deleteById(id);
    }
    @Transactional(readOnly = true)
    public Foerdersatz findOptionalGueltigFuerTypAm(
            VeranstaltungTyp typ,
            LocalDate datum
    ) {
        return repository
                .findGueltigFuerTypAm(typ, datum)
                .orElse(null);
    }
}