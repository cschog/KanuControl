package com.kcserver.repository.finanz;

import com.kcserver.entity.FinanzausgleichZahlung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FinanzausgleichZahlungRepository
        extends JpaRepository<FinanzausgleichZahlung, Long> {

    /**
     * Alle Finanzausgleichzahlungen einer Finanzgruppe.
     */
    List<FinanzausgleichZahlung> findByFinanzGruppeIdOrderByDatumDesc(
            Long finanzGruppeId
    );

    /**
     * Summe aller bereits geleisteten Finanzausgleichzahlungen
     * einer Finanzgruppe.
     */
    @Query("""
            select coalesce(sum(z.betrag), 0)
            from FinanzausgleichZahlung z
            where z.finanzGruppe.id = :finanzGruppeId
            """)
    BigDecimal sumBetragByFinanzGruppeId(
            @Param("finanzGruppeId") Long finanzGruppeId
    );

    /**
     * Lädt eine Zahlung und stellt gleichzeitig sicher,
     * dass sie zur angegebenen Veranstaltung gehört.
     */
    @Query("""
            select z
            from FinanzausgleichZahlung z
            join z.finanzGruppe g
            where z.id = :zahlungId
              and g.veranstaltung.id = :veranstaltungId
            """)
    Optional<FinanzausgleichZahlung> findByIdAndVeranstaltungId(
            @Param("zahlungId") Long zahlungId,
            @Param("veranstaltungId") Long veranstaltungId
    );

    /**
     * Summe aller Finanzausgleichzahlungen einer Veranstaltung.
     */
    @Query("""
            select coalesce(sum(z.betrag), 0)
            from FinanzausgleichZahlung z
            join z.finanzGruppe g
            where g.veranstaltung.id = :veranstaltungId
            """)
    BigDecimal sumBetragByVeranstaltungId(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
        select z
        from FinanzausgleichZahlung z
        join z.finanzGruppe g
        where z.id = :zahlungId
          and g.id = :finanzGruppeId
          and g.veranstaltung.id = :veranstaltungId
        """)
    Optional<FinanzausgleichZahlung> findByIdAndFinanzGruppeIdAndVeranstaltungId(
            @Param("zahlungId") Long zahlungId,
            @Param("finanzGruppeId") Long finanzGruppeId,
            @Param("veranstaltungId") Long veranstaltungId
    );
}