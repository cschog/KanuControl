package com.kcserver.repository;

import com.kcserver.entity.Unterkunftsart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UnterkunftsartRepository extends JpaRepository<Unterkunftsart, Long> {

    Optional<Unterkunftsart> findByBezeichnung(String bezeichnung);

    List<Unterkunftsart> findByAktivTrueOrderByBezeichnungAsc();
}