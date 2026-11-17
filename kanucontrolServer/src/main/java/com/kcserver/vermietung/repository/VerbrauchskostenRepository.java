package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.Verbrauchskosten;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerbrauchskostenRepository
        extends JpaRepository<Verbrauchskosten, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select v from Verbrauchskosten v where v.id = :id")
    java.util.Optional<Verbrauchskosten> findForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

    List<Verbrauchskosten> findByMietobjektIdOrderByIdAsc(Long mietobjektId);
}