package com.kcserver.repository;

import com.kcserver.entity.AbrechnungBeleg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AbrechnungBelegRepository
        extends JpaRepository<AbrechnungBeleg, Long> {

    boolean existsByFinanzGruppe_Id(Long gruppeId);

    List<AbrechnungBeleg> findByAbrechnungId(Long abrechnungId);
}