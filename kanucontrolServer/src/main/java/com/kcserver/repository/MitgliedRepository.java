package com.kcserver.repository;

import com.kcserver.entity.Mitglied;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface MitgliedRepository extends JpaRepository<Mitglied, Long> {

    /* =========================
       READ
       ========================= */

    List<Mitglied> findByPerson_Id(Long personId);

    List<Mitglied> findByVerein_Id(Long vereinId);

    Page<Mitglied> findByPerson_Id(Long personId, Pageable pageable);

    @Query("""
        select m
        from Mitglied m
        join fetch m.verein
        where m.id = :id
    """)
    Optional<Mitglied> findByIdWithVerein(@Param("id") Long id);

    Optional<Mitglied> findByPerson_IdAndHauptVereinTrue(Long personId);

    Optional<Mitglied> findFirstByPerson_IdOrderByIdAsc(Long personId);

    /* =========================
       CONSTRAINTS / CHECKS
       ========================= */

    boolean existsByPerson_IdAndVerein_Id(Long personId, Long vereinId);

    boolean existsByPerson_IdAndHauptVereinTrue(Long personId);

    /* =========================
       ⭐ NEU: HAUPTVEREIN RESET
       ========================= */

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Mitglied m
           set m.hauptVerein = false
         where m.person.id = :personId
           and m.hauptVerein = true
    """)
    void unsetHauptvereinByPerson(@Param("personId") Long personId);
}