package com.kcserver.fahrten.repository;

import com.kcserver.fahrten.entity.FahrtBesatzung;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FahrtBesatzungRepository extends JpaRepository<FahrtBesatzung, Long> {
    List<FahrtBesatzung> findByFahrt_IdOrderById(Long fahrtId);
    List<FahrtBesatzung> findByFahrtKanu_IdOrderById(Long fahrtKanuId);
    boolean existsByTeilnehmer_Id(Long teilnehmerId);
}
