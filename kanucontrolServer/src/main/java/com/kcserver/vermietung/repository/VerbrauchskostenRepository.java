package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.Verbrauchskosten;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerbrauchskostenRepository
        extends JpaRepository<Verbrauchskosten, Long> {

    List<Verbrauchskosten> findByMietobjektIdOrderByIdAsc(Long mietobjektId);
}