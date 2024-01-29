package com.kcserver.service;

import com.kcserver.entity.Mitglied;
import com.kcserver.repository.MitgliedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MitgliedService {
    private final MitgliedRepository mitgliedRepository;

    @Autowired
    public MitgliedService(MitgliedRepository mitgliedRepository) {
        this.mitgliedRepository = mitgliedRepository;
    }

    public Mitglied createMitglied(Mitglied mitglied) {
        return mitgliedRepository.save(mitglied);
    }

    public void deleteMitglied(Mitglied mitglied) {
        mitgliedRepository.delete(mitglied);
    }

    // Other methods for managing Mitglied associations...
}

