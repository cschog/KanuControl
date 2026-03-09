package com.kcserver.service;

import com.kcserver.dto.foerder.FoerdersatzCreateUpdateDTO;
import com.kcserver.dto.foerder.FoerdersatzDTO;
import com.kcserver.entity.Foerdersatz;
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

    /* ========================================================= */

    public FoerdersatzDTO create(FoerdersatzCreateUpdateDTO dto) {

        validateNoOverlap(
                null,
                dto.getGueltigVon(),
                dto.getGueltigBis()
        );

        Foerdersatz entity = new Foerdersatz();
        apply(dto, entity);

        return toDTO(repository.save(entity));
    }

    /* ========================================================= */

    public FoerdersatzDTO update(Long id, FoerdersatzCreateUpdateDTO dto) {

        Foerdersatz entity = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Fördersatz nicht gefunden"
                        ));

        validateNoOverlap(
                id,
                dto.getGueltigVon(),
                dto.getGueltigBis()
        );

        apply(dto, entity);

        return toDTO(repository.save(entity));
    }

    /* ========================================================= */

    public void delete(Long id) {
        repository.deleteById(id);
    }

    /* ========================================================= */

    @Transactional(readOnly = true)
    public FoerdersatzDTO findGueltigAm(LocalDate datum) {

        return repository.findGueltigAm(datum)
                .map(this::toDTO)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Kein gültiger Fördersatz gefunden"
                        ));
    }

    /* ========================================================= */

    private void validateNoOverlap(
            Long ignoreId,
            LocalDate von,
            LocalDate bis
    ) {

        if (von == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "gueltigVon darf nicht null sein"
            );
        }

        boolean exists = repository.existsOverlapping(
                von,
                bis,
                ignoreId
        );

        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Zeitraum überschneidet sich mit bestehendem Fördersatz"
            );
        }
    }

    /* ========================================================= */

    private void apply(FoerdersatzCreateUpdateDTO dto, Foerdersatz entity) {
        entity.setGueltigVon(dto.getGueltigVon());
        entity.setGueltigBis(dto.getGueltigBis());
        entity.setBetragProTeilnehmer(dto.getBetragProTeilnehmer());
        entity.setBeschluss(dto.getBeschluss());
    }

    private FoerdersatzDTO toDTO(Foerdersatz entity) {

        FoerdersatzDTO dto = new FoerdersatzDTO();
        dto.setId(entity.getId());
        dto.setGueltigVon(entity.getGueltigVon());
        dto.setGueltigBis(entity.getGueltigBis());
        dto.setBetragProTeilnehmer(entity.getBetragProTeilnehmer());
        dto.setBeschluss(entity.getBeschluss());

        return dto;
    }
    @Transactional(readOnly = true)
    public FoerdersatzDTO getById(Long id) {

        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Fördersatz nicht gefunden"
                        ));
    }
    @Transactional(readOnly = true)
    public List<FoerdersatzDTO> getAll() {

        return repository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }
}