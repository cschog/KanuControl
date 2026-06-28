package com.kcserver.repository;

import com.kcserver.entity.Verpflegungsmodell;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VerpflegungsmodellRepository extends JpaRepository<Verpflegungsmodell, Long> {

    Optional<Verpflegungsmodell> findByBezeichnung(String bezeichnung);

    List<Verpflegungsmodell> findByAktivTrueOrderByBezeichnungAsc();
}