package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.Buchung;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BuchungRepository extends JpaRepository<Buchung, Long> {

    Optional<Buchung> findTopByBuchungsjahrOrderByLaufendeNummerDesc(
            Integer buchungsjahr
    );

    List<Buchung> findByStatusAndAnreiseLessThanAndAbreiseGreaterThan(
            Buchungsstatus status,
            LocalDateTime abreise,
            LocalDateTime anreise
    );

    List<Buchung> findByStatusAndAnreiseLessThanAndAbreiseGreaterThanAndIdNot(
            Buchungsstatus status,
            LocalDateTime abreise,
            LocalDateTime anreise,
            Long id
    );

    List<Buchung> findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(
            Collection<Buchungsstatus> status,
            LocalDateTime abreise,
            LocalDateTime anreise
    );

    List<Buchung> findByStatusInAndAnreiseLessThanAndAbreiseGreaterThanAndIdNot(
            Collection<Buchungsstatus> status,
            LocalDateTime abreise,
            LocalDateTime anreise,
            Long id
    );

    List<Buchung> findByStatusAndAbreiseLessThanEqual(
            Buchungsstatus status,
            LocalDateTime stichtag
    );
}