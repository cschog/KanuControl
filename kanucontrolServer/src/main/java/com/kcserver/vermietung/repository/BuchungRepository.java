package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.Buchung;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BuchungRepository extends JpaRepository<Buchung, Long> {
    @org.springframework.data.jpa.repository.Query("""
    select distinct b from Buchung b join fetch b.mietbereiche p join fetch p.mietbereich
    where b.status in :status and b.anreise < :bis and b.abreise > :von and b.mietobjekt.id in :objekte
    """)
    List<Buchung> findForConsumptionDashboard(
        @org.springframework.data.repository.query.Param("objekte") java.util.Collection<Long> objekte,
        @org.springframework.data.repository.query.Param("status") java.util.Collection<Buchungsstatus> status,
        @org.springframework.data.repository.query.Param("von") LocalDateTime von,
        @org.springframework.data.repository.query.Param("bis") LocalDateTime bis);

    @org.springframework.data.jpa.repository.Query("""
    select distinct b from Buchung b
    join fetch b.mieter join fetch b.mietobjekt left join fetch b.mietbereiche
    where b.anreise >= :von and b.anreise < :bis
    """)
    java.util.List<Buchung> findForFinanceDashboard(
        @org.springframework.data.repository.query.Param("von") java.time.LocalDateTime von,
        @org.springframework.data.repository.query.Param("bis") java.time.LocalDateTime bis);


    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select b from Buchung b where b.id = :id")
    Optional<Buchung> findLockedById(@org.springframework.data.repository.query.Param("id") Long id);

    List<Buchung> findByMietobjektIdAndStatusInAndAnreiseLessThanAndAbreiseGreaterThan(
            Long mietobjektId, Collection<Buchungsstatus> status, LocalDateTime bis, LocalDateTime von);

    // mieter verweist auf Person, nicht auf den Mieterstammsatz. Alle Status zählen.
    boolean existsByMieterId(Long personId);

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

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    List<Buchung> findByStatusAndAbreiseLessThanEqual(
            Buchungsstatus status,
            LocalDateTime stichtag
    );
}