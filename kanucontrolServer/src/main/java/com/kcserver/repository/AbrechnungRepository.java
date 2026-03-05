package com.kcserver.repository;

import com.kcserver.entity.Abrechnung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AbrechnungRepository
        extends JpaRepository<Abrechnung, Long> {

    Optional<Abrechnung> findByVeranstaltungId(Long veranstaltungId);
}