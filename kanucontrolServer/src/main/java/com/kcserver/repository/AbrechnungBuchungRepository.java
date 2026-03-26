package com.kcserver.repository;

import com.kcserver.entity.AbrechnungBuchung;
import com.kcserver.entity.FinanzGruppe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AbrechnungBuchungRepository
        extends JpaRepository<AbrechnungBuchung, Long> {

    List<AbrechnungBuchung> findByBeleg_Abrechnung_Id(Long abrechnungId);
}