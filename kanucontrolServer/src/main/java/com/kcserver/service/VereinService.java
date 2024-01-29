package com.kcserver.service;

import com.kcserver.entity.Verein;
import com.kcserver.repository.VereinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class VereinService {
    private final VereinRepository vereinRepository;

    @Autowired
    public VereinService(VereinRepository vereinRepository) {
        this.vereinRepository = vereinRepository;
    }

    public List<Verein> getAllVereine() {
        return vereinRepository.findAll();
    }

    public Verein createVerein(Verein verein) {
        return vereinRepository.save(verein);
    }

    public Verein getVerein(@PathVariable long id) {
        return vereinRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Ungültige Verein Id %s", id)));
    }

    public Verein updateVerein(Long vereinId, Verein updatedVerein) {
        Verein existingVerein = vereinRepository.findById(vereinId).orElse(null);
        if (existingVerein != null) {
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
        return null;
    }


    public boolean deleteVerein(long id) {
        if (vereinRepository.existsById(id)) {
            vereinRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
