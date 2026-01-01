package com.kcserver.repository;

import com.kcserver.entity.Erhebungsbogen;
import com.kcserver.entity.Veranstaltung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ErhebungsbogenRepository extends JpaRepository<Erhebungsbogen, Long> {

    Optional<Erhebungsbogen> findByVeranstaltung(Veranstaltung veranstaltung);

    boolean existsByVeranstaltung(Veranstaltung veranstaltung);
}