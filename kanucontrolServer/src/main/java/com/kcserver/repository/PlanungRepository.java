package com.kcserver.repository;

import com.kcserver.entity.Planung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanungRepository extends JpaRepository<Planung, Long> {

    Optional<Planung> findByVeranstaltungId(Long veranstaltungId);
}