package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.BuchungVerbrauchskosten;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuchungVerbrauchskostenRepository
        extends JpaRepository<BuchungVerbrauchskosten, Long> {

    List<BuchungVerbrauchskosten> findByBuchungIdOrderByIdAsc(
            Long buchungId
    );

    void deleteByBuchungId(Long buchungId);
}