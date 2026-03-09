package com.kcserver.service;

import com.kcserver.dto.foerder.FoerdersatzCreateUpdateDTO;
import com.kcserver.dto.foerder.FoerdersatzDTO;
import com.kcserver.entity.Foerdersatz;
import com.kcserver.enumtype.VeranstaltungTyp;
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
                dto.getTyp(),
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
                dto.getTyp(),
                dto.getGueltigVon(),
                dto.getGueltigBis()
        );

        apply(dto, entity);

        return toDTO(repository.save(entity));
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

        boolean exists = repository.existsOverlapping(
                typ,
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

        entity.setTyp(dto.getTyp());
        entity.setGueltigVon(dto.getGueltigVon());
        entity.setGueltigBis(dto.getGueltigBis());
        entity.setFoerdersatz(dto.getFoerdersatz());
        entity.setFoerderdeckel(dto.getFoerderdeckel());
        entity.setBeschluss(dto.getBeschluss());
    }

    /* ========================================================= */

    public FoerdersatzDTO toDTO(Foerdersatz entity) {

        FoerdersatzDTO dto = new FoerdersatzDTO();

        dto.setId(entity.getId());
        dto.setTyp(entity.getTyp());
        dto.setGueltigVon(entity.getGueltigVon());
        dto.setGueltigBis(entity.getGueltigBis());
        dto.setFoerdersatz(entity.getFoerdersatz());
        dto.setFoerderdeckel(entity.getFoerderdeckel());
        dto.setBeschluss(entity.getBeschluss());

        return dto;
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

        return toDTO(entity);
    }
    @Transactional(readOnly = true)
    public List<FoerdersatzDTO> getAll() {

        return repository.findAll()
                .stream()
                .map(this::toDTO)
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
}