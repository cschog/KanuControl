package com.kcserver.repository.abrechnung;

import com.kcserver.dto.zahlungsnachweis.FinanzGruppeZahlungDTO;
import com.kcserver.dto.zahlungsnachweis.ZahlungsnachweisListDTO;
import com.kcserver.entity.Zahlungsnachweis;
import com.kcserver.enumtype.Zahlungsweg;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

public interface ZahlungsnachweisRepository
        extends JpaRepository<Zahlungsnachweis, Long> {

    @EntityGraph(attributePaths = {
            "positionen",
            "positionen.teilnehmer",
            "positionen.teilnehmer.person"
    })
    Optional<Zahlungsnachweis> findByIdAndVeranstaltungId(
            Long id,
            Long veranstaltungId
    );

    @EntityGraph(attributePaths = {
            "dokumente"
    })
    List<Zahlungsnachweis> findByVeranstaltungIdOrderByDatumDescIdDesc(
            Long veranstaltungId
    );

    @Query("""
    select new com.kcserver.dto.zahlungsnachweis.ZahlungsnachweisListDTO(
        z.id,
        z.datum,
        z.betrag,
        z.bemerkung,
        z.zahlungsweg,
        z.finanzGruppe.id,

        (select count(p)
         from ZahlungsPosition p
         where p.zahlungsnachweis.id = z.id),

        (select count(d)
         from Dokument d
         where d.zahlungsnachweis.id = z.id)
    )
    from Zahlungsnachweis z
    where z.veranstaltung.id = :veranstaltungId
    order by z.datum desc, z.id desc
""")
    List<ZahlungsnachweisListDTO> findListByVeranstaltungId(
            @Param("veranstaltungId") Long veranstaltungId
    );

    boolean existsByPositionenTeilnehmerId(Long teilnehmerId);


    void deleteByIdAndVeranstaltungId(
            Long id,
            Long veranstaltungId
    );

    @Query("""
    select
        p.teilnehmer.id as teilnehmerId,
        coalesce(sum(p.betrag), 0) as gezahlterBetrag
    from ZahlungsPosition p
    where p.teilnehmer.veranstaltung.id = :veranstaltungId
    group by p.teilnehmer.id
""")
    List<TeilnehmerZahlungSumme> summeZahlungenByVeranstaltung(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
    select coalesce(sum(p.betrag), 0)
    from ZahlungsPosition p
    where p.teilnehmer.id = :teilnehmerId
      and (:ausgeschlossenId is null
           or p.zahlungsnachweis.id <> :ausgeschlossenId)
""")
    BigDecimal sumBetragByTeilnehmerId(
            @Param("teilnehmerId") Long teilnehmerId,
            @Param("ausgeschlossenId") Long ausgeschlossenId
    );

    @Query("""
    SELECT
        z.finanzGruppe.id,
        COALESCE(SUM(z.betrag), 0)
    FROM Zahlungsnachweis z
    WHERE z.veranstaltung.id = :veranstaltungId
    GROUP BY z.finanzGruppe.id
""")
    List<Object[]> sumBetragByFinanzGruppeGrouped(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
    select new com.kcserver.dto.zahlungsnachweis.FinanzGruppeZahlungDTO(
        z.id,
        z.datum,
        z.betrag,
        z.zahlungsweg,
        z.bemerkung,

        (select count(d)
         from Dokument d
         where d.zahlungsnachweis.id = z.id)
    )
    from Zahlungsnachweis z
    where z.veranstaltung.id = :veranstaltungId
      and z.finanzGruppe.id = :finanzGruppeId
    order by z.datum desc, z.id desc
""")
    List<FinanzGruppeZahlungDTO> findZahlungenByFinanzGruppe(
            @Param("veranstaltungId") Long veranstaltungId,
            @Param("finanzGruppeId") Long finanzGruppeId
    );

    @Query("""
    SELECT
        p.teilnehmer.finanzGruppe.id,
        COALESCE(SUM(p.betrag), 0)
    FROM ZahlungsPosition p
    WHERE p.teilnehmer.veranstaltung.id = :veranstaltungId
    GROUP BY p.teilnehmer.finanzGruppe.id
""")
    List<Object[]> sumPositionBetragByFinanzGruppeGrouped(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
    select coalesce(sum(z.betrag), 0)
    from Zahlungsnachweis z
    where z.veranstaltung.id = :veranstaltungId
      and z.zahlungsweg = :zahlungsweg
""")
    BigDecimal sumBetragByVeranstaltungAndZahlungsweg(
            @Param("veranstaltungId") Long veranstaltungId,
            @Param("zahlungsweg") Zahlungsweg zahlungsweg
    );

}