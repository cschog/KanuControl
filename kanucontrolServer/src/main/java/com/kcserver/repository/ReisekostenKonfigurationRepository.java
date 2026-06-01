package com.kcserver.repository;

import com.kcserver.entity.ReisekostenKonfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ReisekostenKonfigurationRepository
        extends JpaRepository<ReisekostenKonfiguration, Long> {

    Optional<ReisekostenKonfiguration>
    findFirstByGueltigBisIsNull();

    Optional<ReisekostenKonfiguration>
    findFirstByGueltigVonLessThanEqualAndGueltigBisGreaterThanEqual(
            LocalDate datum1,
            LocalDate datum2
    );
    Optional<ReisekostenKonfiguration>
    findFirstByGueltigVonLessThanEqualAndGueltigBisIsNull(
            LocalDate datum
    );
}