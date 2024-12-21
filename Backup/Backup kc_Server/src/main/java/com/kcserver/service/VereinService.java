package com.kcserver.service;

import com.kcserver.dto.VereinDTO;
import com.kcserver.entity.Verein;
import com.kcserver.repository.VereinRepository;
import com.kcserver.mapper.EntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VereinService {

    private final VereinRepository vereinRepository;
    private final EntityMapper mapper;

    @Autowired
    public VereinService(VereinRepository vereinRepository, EntityMapper mapper) {
        this.vereinRepository = vereinRepository;
        this.mapper = mapper;
    }

    /**
     * Retrieve all Vereine as VereinDTOs.
     *
     * @return List of VereinDTOs.
     */
    public List<VereinDTO> getAllVereine() {
        return vereinRepository.findAll().stream()
                .map(mapper::toVereinDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a Verein by its ID and return as VereinDTO.
     *
     * @param id The ID of the Verein.
     * @return The VereinDTO.
     * @throws ResponseStatusException if the Verein is not found.
     */
    public VereinDTO getVerein(long id) {
        Verein verein = vereinRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verein not found"));
        return mapper.toVereinDTO(verein);
    }

    /**
     * Retrieve a Verein entity by its ID.
     *
     * @param id The ID of the Verein.
     * @return The Verein entity.
     * @throws ResponseStatusException if the Verein is not found.
     */
    public Verein getVereinEntityById(long id) {
        return vereinRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verein not found"));
    }

    /**
     * Create a new Verein from VereinDTO.
     *
     * @param vereinDTO The VereinDTO to be created.
     * @return The created VereinDTO.
     */
    public VereinDTO createVerein(VereinDTO vereinDTO) {
        Verein verein = mapper.toVereinEntity(vereinDTO);
        Verein savedVerein = vereinRepository.save(verein);
        return mapper.toVereinDTO(savedVerein);
    }

    /**
     * Update an existing Verein by its ID using VereinDTO.
     *
     * @param id        The ID of the Verein to be updated.
     * @param vereinDTO The updated VereinDTO data.
     * @return The updated VereinDTO.
     * @throws ResponseStatusException if the Verein is not found.
     */
    public VereinDTO updateVerein(long id, VereinDTO vereinDTO) {
        Verein existingVerein = vereinRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verein not found"));

        mapper.updateVereinFromDTO(vereinDTO, existingVerein);

        Verein updatedVerein = vereinRepository.save(existingVerein);
        return mapper.toVereinDTO(updatedVerein);
    }

    /**
     * Delete a Verein by its ID.
     *
     * @param id The ID of the Verein to delete.
     * @return true if successful, false otherwise.
     */
    public boolean deleteVerein(long id) {
        if (vereinRepository.existsById(id)) {
            vereinRepository.deleteById(id);
            return true;
        }
        return false;
    }
}