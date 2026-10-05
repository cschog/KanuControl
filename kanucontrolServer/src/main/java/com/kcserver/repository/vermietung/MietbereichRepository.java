package com.kcserver.repository.vermietung;

import com.kcserver.entity.vermietung.Mietbereich;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MietbereichRepository
        extends JpaRepository<Mietbereich, Long> {

    List<Mietbereich> findByMietobjektId(Long mietobjektId);
}