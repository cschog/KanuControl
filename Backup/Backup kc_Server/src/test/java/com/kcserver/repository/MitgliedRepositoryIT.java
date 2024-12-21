package com.kcserver.repository;

import com.kcserver.entity.Mitglied;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class MitgliedRepositoryIT {

    @Autowired
    private MitgliedRepository mitgliedRepository;

    @Test
    public void testFindByVereinId() {
        List<Mitglied> results = mitgliedRepository.findByVereinMitgliedschaft_Id(1L);
        assertNotNull(results, "Results should not be null");
        assertFalse(results.isEmpty(), "Results should not be empty");
    }
}
