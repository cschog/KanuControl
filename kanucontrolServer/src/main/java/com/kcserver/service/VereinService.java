package com.kcserver.service;

import com.kcserver.dto.verein.VereinDTO;
import com.kcserver.dto.verein.VereinRefDTO;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.mapper.VereinMapper;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VereinRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    @Transactional(readOnly = true)
    public List<VereinDTO> search(String name, String abk, Pageable pageable) {

        Specification<Verein> spec = Specification.allOf(
                name != null && !name.isBlank()
                        ? (root, query, cb) ->
                        cb.like(
                                cb.lower(root.get("name")),
                                "%" + name.toLowerCase() + "%"
                        )
                        : null,

                abk != null && !abk.isBlank()
                        ? (root, query, cb) ->
                        cb.equal(root.get("abk"), abk)
                        : null
        );

        return vereinRepository.findAll(spec, pageable)
                .map(vereinMapper::toDTO)
                .getContent();
    }

    @Transactional(readOnly = true)
    public Page<VereinRefDTO> searchRef(String search, Pageable pageable) {

        Page<Verein> page;

        if (search == null || search.isBlank()) {
            page = vereinRepository.findAll(pageable);
        } else {
            page = vereinRepository
                    .findByNameContainingIgnoreCaseOrAbkContainingIgnoreCase(
                            search, search, pageable
                    );
        }

        return page.map(v -> new VereinRefDTO(
                v.getId(),
                v.getName(),
                v.getAbk()
        ));
    }

    /* =========================================================
       CREATE
       ========================================================= */

    public VereinDTO create(VereinDTO dto) {

        normalize(dto);

        // 🔒 Fachliche Duplikatsprüfung
        if (vereinRepository.existsByAbkAndName(dto.getAbk(), dto.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Verein mit Abkürzung und Name existiert bereits"
            );
        }

        Verein verein = vereinMapper.toEntity(dto);

        if (dto.getKontoinhaberId() != null) {
            Person kontoinhaber = personRepository.findById(dto.getKontoinhaberId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Kontoinhaber not found"
                    ));
            verein.setKontoinhaber(kontoinhaber);
        }

        return vereinMapper.toDTO(
                vereinRepository.save(verein)
        );
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    public VereinDTO update(Long id, VereinDTO dto) {

        normalize(dto);

        Verein verein = vereinRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Verein not found"
                ));

        // 🔒 Fachliche Duplikatsprüfung (UPDATE!)
        if (vereinRepository.existsByAbkAndName(dto.getAbk(), dto.getName())) {

            boolean sameEntity =
                    verein.getAbk().equals(dto.getAbk()) &&
                            verein.getName().equals(dto.getName());

            if (!sameEntity) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Verein mit Abkürzung und Name existiert bereits"
                );
            }
        }

        vereinMapper.updateFromDTO(dto, verein);

        // Kontoinhaber entfernen
        if (dto.getKontoinhaberId() == null) {
            verein.setKontoinhaber(null);
        }
        // Kontoinhaber setzen oder wechseln
        else if (
                verein.getKontoinhaber() == null ||
                        !verein.getKontoinhaber().getId().equals(dto.getKontoinhaberId())
        ) {
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

    private void normalize(VereinDTO dto) {
        if (dto.getName() != null) {
            dto.setName(dto.getName().trim());
        }
        if (dto.getAbk() != null) {
            dto.setAbk(dto.getAbk().trim());
        }
    }
}