package com.kcserver.service;

import com.kcserver.entity.Verein;
import com.kcserver.repository.VereinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class VereinService {

    private final VereinRepository vereinRepository;

    @Autowired
    public VereinService(VereinRepository vereinRepository) {
        this.vereinRepository = vereinRepository;
    }

    /**
     * Retrieves all Verein entities.
     *
     * @return a list of all Vereine
     */
    public List<Verein> getAllVereine() {
        return vereinRepository.findAll();
    }

    /**
     * Retrieves a Verein by its unique ID.
     *
     * @param id the ID of the Verein
     * @return the verein object
     * @throws ResponseStatusException if the Verein with the given ID is not found
     */
    public Verein getVerein(long id) {
        return vereinRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Verein with ID %s not found", id)));
    }

    /**
     * Retrieves a Verein by its exact name.
     *
     * @param name the name of the Verein
     * @return the verein object
     * @throws ResponseStatusException if no Verein with the given name is found
     */
    public Verein getVereinByName(String name) {
        return Optional.ofNullable(vereinRepository.findByNameIs(name))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Verein with name %s not found", name)));
    }

    /**
     * Creates a new Verein.
     *
     * @param verein the Verein object to be saved
     * @return the saved Verein
     */
    public Verein createVerein(Verein verein) {
        return vereinRepository.save(verein);
    }

    /**
     * Updates an existing Verein.
     *
     * @param vereinId      the ID of the Verein to be updated
     * @param updatedVerein the updated Verein object
     * @return the updated Verein, or null if not found
     * @throws ResponseStatusException if the Verein with the given ID doesn't exist
     */
    public Verein updateVerein(long vereinId, Verein updatedVerein) {
        Verein existingVerein = getVerein(vereinId); // Using the getVerein method to ensure the Verein exists
        existingVerein.setName(updatedVerein.getName());
        existingVerein.setAbk(updatedVerein.getAbk());
        existingVerein.setStrasse(updatedVerein.getStrasse());
        existingVerein.setPlz(updatedVerein.getPlz());
        existingVerein.setOrt(updatedVerein.getOrt());
        existingVerein.setTelefon(updatedVerein.getTelefon());
        existingVerein.setBankName(updatedVerein.getBankName());
        existingVerein.setKontoInhaber(updatedVerein.getKontoInhaber());
        existingVerein.setKiAnschrift(updatedVerein.getKiAnschrift());
        existingVerein.setIban(updatedVerein.getIban());
        existingVerein.setBic(updatedVerein.getBic());
        return vereinRepository.save(existingVerein);
    }

    /**
     * Deletes a Verein by its ID.
     *
     * @param id the ID of the Verein to be deleted
     * @return true if the Verein was deleted successfully, otherwise false
     */
    public boolean deleteVerein(long id) {
        if (vereinRepository.existsById(id)) {
            vereinRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Finds Vereine by their name.
     *
     * @param name the name to search for
     * @return a list of Vereine with the specified name
     */
    public List<Verein> findVereineByName(String name) {
        return vereinRepository.findByName(name);
    }

    /**
     * Finds Vereine by their postal code.
     *
     * @param plz the postal code to search for
     * @return a list of Vereine in the specified postal code
     */
    public List<Verein> findVereineByPlz(String plz) {
        return vereinRepository.findByPlz(plz);
    }

    /**
     * Finds Vereine by their city.
     *
     * @param ort the city to search for
     * @return a list of Vereine in the specified city
     */
    public List<Verein> findVereineByOrt(String ort) {
        return vereinRepository.findByOrt(ort);
    }
}