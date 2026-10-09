package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.BuchungMietbereich;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BuchungMietbereichRepository
        extends JpaRepository<BuchungMietbereich, Long> {

    List<BuchungMietbereich> findByBuchungId(Long buchungId);
}