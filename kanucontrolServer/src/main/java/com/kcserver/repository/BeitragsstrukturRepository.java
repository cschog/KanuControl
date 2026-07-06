package com.kcserver.repository;

import com.kcserver.entity.Beitragsstruktur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface BeitragsstrukturRepository
        extends JpaRepository<Beitragsstruktur, Long> {

    List<Beitragsstruktur> findByTemplateTrue();

    List<Beitragsstruktur> findByTemplateFalse();

    boolean existsByTemplateTrue();

    Optional<Beitragsstruktur> findFirstByTemplateTrue();

    @Query("""
    select distinct b
    from Beitragsstruktur b
    left join fetch b.regeln
    where b.id = :id
""")
    Optional<Beitragsstruktur> findWithRegelnById(Long id);
}