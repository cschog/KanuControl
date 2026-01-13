package com.kcserver.repository;

import com.kcserver.entity.Mitglied;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MitgliedRepository extends JpaRepository<Mitglied, Long> {

    // Read
    List<Mitglied> findByPerson_Id(Long personId);

    List<Mitglied> findByVerein_Id(Long vereinId);

    Optional<Mitglied> findByPerson_IdAndHauptVereinTrue(Long personId);

    // Constraints / Checks
    boolean existsByPerson_IdAndVerein_Id(Long personId, Long vereinId);

    boolean existsByPerson_IdAndHauptVereinTrue(Long personId);
}