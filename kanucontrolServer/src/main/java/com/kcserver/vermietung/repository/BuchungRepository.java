package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.Buchung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BuchungRepository extends JpaRepository<Buchung, Long> {

    Optional<Buchung> findTopByBuchungsjahrOrderByLaufendeNummerDesc(
            Integer buchungsjahr
    );

}