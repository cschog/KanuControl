package com.kcserver.repository.abrechnung;

import com.kcserver.entity.AbrechnungBeleg;
import com.kcserver.entity.AbrechnungBuchung;
import com.kcserver.enumtype.BuchungsHerkunft;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AbrechnungBuchungRepository
        extends JpaRepository<AbrechnungBuchung, Long> {

    @EntityGraph(attributePaths = {
            "beleg",
            "beleg.finanzGruppe"
    })
    List<AbrechnungBuchung> findByBeleg_Abrechnung_Id(Long abrechnungId);

    @Query("""
select count(b) > 0
from AbrechnungBuchung b
where b.beleg.abrechnung.veranstaltung.id = :veranstaltungId
and b.betrag > 0
and b.kategorie in (
    com.kcserver.enumtype.FinanzKategorie.UNTERKUNFT,
    com.kcserver.enumtype.FinanzKategorie.VERPFLEGUNG,
    com.kcserver.enumtype.FinanzKategorie.HONORARE,
    com.kcserver.enumtype.FinanzKategorie.FAHRTKOSTEN,
    com.kcserver.enumtype.FinanzKategorie.VERBRAUCHSMATERIAL,
    com.kcserver.enumtype.FinanzKategorie.KULTUR,
    com.kcserver.enumtype.FinanzKategorie.MIETE,
    com.kcserver.enumtype.FinanzKategorie.SONSTIGE_KOSTEN
)
""")
    boolean existsKosten(Long veranstaltungId);

    @Query("""
select count(b) > 0
from AbrechnungBuchung b
where b.beleg.abrechnung.veranstaltung.id = :veranstaltungId
and b.betrag > 0
and b.kategorie in (
    com.kcserver.enumtype.FinanzKategorie.TEILNEHMERBEITRAG,
    com.kcserver.enumtype.FinanzKategorie.PFAND,
    com.kcserver.enumtype.FinanzKategorie.KJFP_ZUSCHUSS,
    com.kcserver.enumtype.FinanzKategorie.SONSTIGE_EINNAHMEN
)
""")
    boolean existsEinnahmen(Long veranstaltungId);

    boolean existsByBeleg_Abrechnung_Veranstaltung_Id(Long veranstaltungId);

    void deleteByBelegAndHerkunft(
            AbrechnungBeleg beleg,
            BuchungsHerkunft herkunft
    );

    @Query("""
    SELECT
        b.beleg.finanzGruppe.id,
        COALESCE(SUM(
            CASE
                WHEN b.betrag > 0
                AND b.kategorie IN (
                    com.kcserver.enumtype.FinanzKategorie.PFAND,
                    com.kcserver.enumtype.FinanzKategorie.KJFP_ZUSCHUSS,
                    com.kcserver.enumtype.FinanzKategorie.SONSTIGE_EINNAHMEN
                )
                THEN b.betrag
                ELSE 0
            END
        ), 0),
        COALESCE(SUM(
            CASE
                WHEN b.betrag > 0
                AND b.kategorie IN (
                    com.kcserver.enumtype.FinanzKategorie.UNTERKUNFT,
                    com.kcserver.enumtype.FinanzKategorie.VERPFLEGUNG,
                    com.kcserver.enumtype.FinanzKategorie.HONORARE,
                    com.kcserver.enumtype.FinanzKategorie.FAHRTKOSTEN,
                    com.kcserver.enumtype.FinanzKategorie.VERBRAUCHSMATERIAL,
                    com.kcserver.enumtype.FinanzKategorie.KULTUR,
                    com.kcserver.enumtype.FinanzKategorie.MIETE,
                    com.kcserver.enumtype.FinanzKategorie.SONSTIGE_KOSTEN
                )
                THEN b.betrag
                ELSE 0
            END
        ), 0)
    FROM AbrechnungBuchung b
    WHERE b.beleg.abrechnung.veranstaltung.id = :veranstaltungId
    GROUP BY b.beleg.finanzGruppe.id
""")
    List<Object[]> sumFinanzenByVeranstaltungGrouped(Long veranstaltungId);

    @Query("""
    select coalesce(sum(-b.betrag), 0)
    from AbrechnungBuchung b
    where b.urspruenglicherZahlungsnachweis.id = :zahlungsnachweisId
      and b.kategorie =
          com.kcserver.enumtype.FinanzKategorie.TEILNEHMERBEITRAG
      and b.betrag < 0
""")
    BigDecimal sumZurueckgezahltByUrspruenglichemZahlungsnachweisId(
            @Param("zahlungsnachweisId")
            Long zahlungsnachweisId
    );

    @Query("""
    SELECT
        b.urspruenglicherZahlungsnachweis.id
            AS zahlungsnachweisId,

        COALESCE(SUM(-b.betrag), 0)
            AS zurueckgezahlt

    FROM AbrechnungBuchung b

    WHERE b.beleg.abrechnung.veranstaltung.id = :veranstaltungId
      AND b.urspruenglicherZahlungsnachweis IS NOT NULL
      AND b.kategorie =
          com.kcserver.enumtype.FinanzKategorie.TEILNEHMERBEITRAG
      AND b.betrag < 0

    GROUP BY b.urspruenglicherZahlungsnachweis.id
""")
    List<ZahlungsnachweisRueckzahlungSumme>
    sumZurueckgezahltByVeranstaltungGrouped(
            @Param("veranstaltungId") Long veranstaltungId
    );
}