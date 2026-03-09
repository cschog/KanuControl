package com.kcserver.repository;

import com.kcserver.entity.FinanzGruppe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinanzGruppeRepository extends JpaRepository<FinanzGruppe, Long> {

    List<FinanzGruppe> findByVeranstaltungIdOrderByKuerzel(Long veranstaltungId);

    Optional<FinanzGruppe> findByVeranstaltungIdAndKuerzel(Long veranstaltungId, String kuerzel);

    boolean existsByVeranstaltungIdAndKuerzel(Long veranstaltungId, String kuerzel);


}