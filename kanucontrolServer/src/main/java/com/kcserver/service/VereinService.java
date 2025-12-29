package com.kcserver.service;

import com.kcserver.dto.VereinDTO;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.mapper.VereinMapper;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VereinRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class VereinService {

    private final VereinRepository vereinRepository;
    private final PersonRepository personRepository;
    private final VereinMapper vereinMapper;

    public VereinService(
            VereinRepository vereinRepository,
            PersonRepository personRepository,
            VereinMapper vereinMapper
    ) {
        this.vereinRepository = vereinRepository;
        this.personRepository = personRepository;
        this.vereinMapper = vereinMapper;
    }

    /* =========================================================
       READ
       ========================================================= */

    @Transactional(readOnly = true)
    public List<VereinDTO> getAll() {
        return vereinRepository.findAll()
                .stream()
                .map(vereinMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public VereinDTO getById(Long id) {
        Verein verein = vereinRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Verein not found"
                ));
        return vereinMapper.toDTO(verein);
    }

    /* =========================================================
       CREATE
       ========================================================= */

    public VereinDTO create(VereinDTO dto) {

        Person kontoinhaber = personRepository.findById(dto.getKontoinhaberId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Kontoinhaber not found"
                ));

        Verein verein = vereinMapper.toEntity(dto);
        verein.setKontoinhaber(kontoinhaber);

        return vereinMapper.toDTO(
                vereinRepository.save(verein)
        );
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    public VereinDTO update(Long id, VereinDTO dto) {

        Verein verein = vereinRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Verein not found"
                ));

        vereinMapper.updateFromDTO(dto, verein);

        if (!verein.getKontoinhaber().getId().equals(dto.getKontoinhaberId())) {
            Person neuerInhaber = personRepository.findById(dto.getKontoinhaberId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Kontoinhaber not found"
                    ));
            verein.setKontoinhaber(neuerInhaber);
        }

        return vereinMapper.toDTO(verein);
    }

    /* =========================================================
       DELETE
       ========================================================= */

    public void delete(Long id) {
        if (!vereinRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Verein not found"
            );
        }
        vereinRepository.deleteById(id);
    }
}