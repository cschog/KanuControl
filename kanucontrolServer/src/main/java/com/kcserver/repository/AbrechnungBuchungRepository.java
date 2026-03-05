package com.kcserver.repository;

import com.kcserver.entity.AbrechnungBuchung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AbrechnungBuchungRepository
        extends JpaRepository<AbrechnungBuchung, Long> {

    List<AbrechnungBuchung> findByAbrechnungId(Long abrechnungId);
}