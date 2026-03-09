package com.kcserver.repository;

import com.kcserver.entity.Foerdersatz;
import com.kcserver.enumtype.VeranstaltungTyp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface FoerdersatzRepository
        extends JpaRepository<Foerdersatz, Long> {

    /* =========================================================
       Gültig für Typ + Datum
       ========================================================= */

    @Query("""
        select f from Foerdersatz f
        where
            f.typ = :typ
        and
            f.gueltigVon <= :datum
        and
            (f.gueltigBis is null or f.gueltigBis >= :datum)
        order by f.gueltigVon desc
    """)
    Optional<Foerdersatz> findGueltigFuerTypAm(
            @Param("typ") VeranstaltungTyp typ,
            @Param("datum") LocalDate datum
    );

    /* =========================================================
       Überlappungsprüfung (typabhängig!)
       ========================================================= */

    @Query("""
    select count(f) > 0
    from Foerdersatz f
    where
        f.typ = :typ
    and
        (:ignoreId is null or f.id <> :ignoreId)
    and
        f.gueltigVon <= coalesce(:bis, f.gueltigVon)
    and
        coalesce(f.gueltigBis, :von) >= :von
""")
    boolean existsOverlapping(
            @Param("typ") VeranstaltungTyp typ,
            @Param("von") LocalDate von,
            @Param("bis") LocalDate bis,
            @Param("ignoreId") Long ignoreId
    );
}