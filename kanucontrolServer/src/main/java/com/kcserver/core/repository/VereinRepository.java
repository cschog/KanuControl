package com.kcserver.core.repository;

import com.kcserver.core.entity.Verein;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface VereinRepository
        extends JpaRepository<Verein, Long>,
        JpaSpecificationExecutor<Verein> {

    /* =========================================================
       BUSINESS CHECKS
       ========================================================= */

    boolean existsByAbkAndName(String abk, String name);

    Optional<Verein> findByAbkAndName(String abk, String name);

    /* =========================================================
       REF LIST (Dropdown / Autocomplete)
       ========================================================= */

    @Query("""
        SELECT v
        FROM Verein v
        WHERE v.kjfpVerein = true AND (
            :search IS NULL
            OR :search = ''
            OR LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(v.abk) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        ORDER BY v.name
    """)
    List<Verein> searchRefList(@Param("search") String search);

    /**
     * Findet den bisherigen Hausverein.
     */
    Optional<Verein> findByHausvereinTrue();

    /**
     * Setzt hausverein für alle Vereine zurück.
     */
    @Modifying
    @Query("""
    UPDATE Verein v
    SET v.hausverein = false
    WHERE v.hausverein = true
""")
    int unsetHausverein();
}