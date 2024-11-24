package com.kcserver.service;

import com.kcserver.dto.VereinDTO;
import com.kcserver.entity.Verein;
import com.kcserver.repository.VereinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VereinService {

    private final VereinRepository vereinRepository;

    @Autowired
    public VereinService(VereinRepository vereinRepository) {
        this.vereinRepository = vereinRepository;
    }

    /**
     * Retrieve all Vereine as VereinDTOs.
     *
     * @return List of VereinDTOs.
     */
    public List<VereinDTO> getAllVereine() {
        return vereinRepository.findAll().stream()
                .map(this::convertToDTO)
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
        return convertToDTO(verein);
    }

    /**
     * Create a new Verein from VereinDTO.
     *
     * @param vereinDTO The VereinDTO to be created.
     * @return The created VereinDTO.
     */
    public VereinDTO createVerein(VereinDTO vereinDTO) {
        Verein verein = convertToEntity(vereinDTO);
        Verein savedVerein = vereinRepository.save(verein);
        return convertToDTO(savedVerein);
    }


    /**
     * Update an existing Verein by its ID using VereinDTO.
     *
     * @param id The ID of the Verein to be updated.
     * @param vereinDTO The updated VereinDTO data.
     * @return The updated VereinDTO.
     * @throws ResponseStatusException if the Verein is not found.
     */
    public VereinDTO updateVerein(long id, VereinDTO vereinDTO) {
        Verein existingVerein = vereinRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verein not found"));

        existingVerein.setName(vereinDTO.getName());
        existingVerein.setAbk(vereinDTO.getAbk());
        existingVerein.setStrasse(vereinDTO.getStrasse());
        existingVerein.setPlz(vereinDTO.getPlz());
        existingVerein.setOrt(vereinDTO.getOrt());
        existingVerein.setTelefon(vereinDTO.getTelefon());
        existingVerein.setBankName(vereinDTO.getBankName());
        existingVerein.setKontoInhaber(vereinDTO.getKontoInhaber());
        existingVerein.setKiAnschrift(vereinDTO.getKiAnschrift());
        existingVerein.setIban(vereinDTO.getIban());
        existingVerein.setBic(vereinDTO.getBic());

        Verein updatedVerein = vereinRepository.save(existingVerein);
        return convertToDTO(updatedVerein);
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

    /**
     * Converts a Verein entity to a VereinDTO.
     *
     * @param verein The Verein entity.
     * @return The corresponding VereinDTO.
     */
    private VereinDTO convertToDTO(Verein verein) {
        return new VereinDTO(
                verein.getId(),
                verein.getName(),
                verein.getAbk(),
                verein.getStrasse(),
                verein.getPlz(),
                verein.getOrt(),
                verein.getTelefon(),
                verein.getBankName(),
                verein.getKontoInhaber(),
                verein.getKiAnschrift(),
                verein.getIban(),
                verein.getBic()
        );
    }

    /**
     * Converts a VereinDTO to a Verein entity.
     *
     * @param vereinDTO The VereinDTO.
     * @return The corresponding Verein entity.
     */
    public Verein convertToEntity(VereinDTO vereinDTO) {
        return new Verein(
                vereinDTO.getName(),
                vereinDTO.getAbk(),
                vereinDTO.getStrasse(),
                vereinDTO.getPlz(),
                vereinDTO.getOrt(),
                vereinDTO.getTelefon(),
                vereinDTO.getBankName(),
                vereinDTO.getKontoInhaber(),
                vereinDTO.getKiAnschrift(),
                vereinDTO.getIban(),
                vereinDTO.getBic()
        );
    }
}
