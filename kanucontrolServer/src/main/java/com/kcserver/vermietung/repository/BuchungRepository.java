package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.Buchung;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BuchungRepository extends JpaRepository<Buchung, Long> {

    Optional<Buchung> findTopByBuchungsjahrOrderByLaufendeNummerDesc(
            Integer buchungsjahr
    );

    List<Buchung> findByStatusAndAnreiseLessThanAndAbreiseGreaterThan(
            Buchungsstatus status,
            LocalDate abreise,
            LocalDate anreise
    );

    List<Buchung> findByStatusAndAnreiseLessThanAndAbreiseGreaterThanAndIdNot(
            Buchungsstatus status,
            LocalDate abreise,
            LocalDate anreise,
            Long id
    );
}