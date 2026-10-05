package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.Mietbereich;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MietbereichRepository
        extends JpaRepository<Mietbereich, Long> {

    List<Mietbereich> findByMietobjektId(Long mietobjektId);
}