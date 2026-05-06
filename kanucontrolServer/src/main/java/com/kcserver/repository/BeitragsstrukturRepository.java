package com.kcserver.repository;

import com.kcserver.entity.Beitragsstruktur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BeitragsstrukturRepository
        extends JpaRepository<Beitragsstruktur, Long> {

    List<Beitragsstruktur> findByTemplateTrue();

    List<Beitragsstruktur> findByTemplateFalse();

    boolean existsByTemplateTrue();

    Optional<Beitragsstruktur> findFirstByTemplateTrue();
}