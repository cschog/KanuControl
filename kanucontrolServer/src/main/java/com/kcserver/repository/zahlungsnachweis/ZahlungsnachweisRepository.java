package com.kcserver.repository.zahlungsnachweis;

import com.kcserver.dto.zahlungsnachweis.FinanzGruppeZahlungDTO;
import com.kcserver.dto.zahlungsnachweis.OffeneUeberzahlungDTO;
import com.kcserver.dto.zahlungsnachweis.ZahlungsnachweisListDTO;
import com.kcserver.entity.Zahlungsnachweis;
import com.kcserver.enumtype.Zahlungsweg;
import com.kcserver.repository.abrechnung.TeilnehmerZahlungSumme;
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
         where d.zahlungsnachweis.id = z.id),

        case
            when z.urspruenglicherZahlungsnachweis is not null
            then true
            else false
        end
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
      AND z.urspruenglicherZahlungsnachweis IS NULL
    GROUP BY z.finanzGruppe.id
""")
    List<Object[]> sumBetragByFinanzGruppeGrouped(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
    select
        z.finanzGruppe.id,
        coalesce(sum(z.betrag), 0)
    from Zahlungsnachweis z
    where z.veranstaltung.id = :veranstaltungId
      and z.urspruenglicherZahlungsnachweis is null
      and z.zahlungsweg =
          com.kcserver.enumtype.Zahlungsweg.QUITTUNG
    group by z.finanzGruppe.id
""")
    List<Object[]> sumQuittungenByFinanzGruppeGrouped(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
    select
        p.teilnehmer.finanzGruppe.id,
        coalesce(sum(p.betrag), 0)
    from ZahlungsPosition p
    join p.zahlungsnachweis z
    where z.veranstaltung.id = :veranstaltungId
      and z.zahlungsweg =
          com.kcserver.enumtype.Zahlungsweg.UEBERWEISUNG
      and p.teilnehmer.finanzGruppe is not null
    group by p.teilnehmer.finanzGruppe.id
""")
    List<Object[]> sumUeberweisungenByTeilnehmerFinanzGruppeGrouped(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
    select
        z.ueberzahlungsFinanzGruppe.id,
        coalesce(
            sum(
                z.betrag
                - coalesce(
                    (
                        select sum(p.betrag)
                        from ZahlungsPosition p
                        where p.zahlungsnachweis.id = z.id
                    ),
                    0
                )
            ),
            0
        )
    from Zahlungsnachweis z
    where z.veranstaltung.id = :veranstaltungId
      and z.urspruenglicherZahlungsnachweis is null
      and z.zahlungsweg =
          com.kcserver.enumtype.Zahlungsweg.UEBERWEISUNG
      and z.ueberzahlungsFinanzGruppe is not null
    group by z.ueberzahlungsFinanzGruppe.id
""")
    List<Object[]> sumUeberzahlungsUeberweisungenByFinanzGruppeGrouped(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
    select
        p.teilnehmer.finanzGruppe.id,
        coalesce(sum(p.betrag), 0)
    from ZahlungsPosition p
    join p.zahlungsnachweis z
    where z.veranstaltung.id = :veranstaltungId
      and z.zahlungsweg =
          com.kcserver.enumtype.Zahlungsweg.QUITTUNG
      and p.teilnehmer.finanzGruppe is not null
    group by p.teilnehmer.finanzGruppe.id
""")
    List<Object[]> sumQuittungenByTeilnehmerFinanzGruppeGrouped(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
    select
        z.finanzGruppe.id,
        z.zahlungsweg,
        coalesce(sum(z.betrag), 0)
    from Zahlungsnachweis z
    where z.veranstaltung.id = :veranstaltungId
      and z.urspruenglicherZahlungsnachweis is null
    group by
        z.finanzGruppe.id,
        z.zahlungsweg
""")
    List<Object[]> sumBetragByFinanzGruppeAndZahlungswegGrouped(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
    select
        p.teilnehmer.finanzGruppe.id,
        z.zahlungsweg,
        coalesce(sum(p.betrag), 0)
    from ZahlungsPosition p
    join p.zahlungsnachweis z
    where z.veranstaltung.id = :veranstaltungId
      and p.teilnehmer.finanzGruppe is not null
    group by
        p.teilnehmer.finanzGruppe.id,
        z.zahlungsweg
""")
    List<Object[]> sumTeilnehmerBeitraegeByFinanzGruppeAndZahlungsweg(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
    select new com.kcserver.dto.zahlungsnachweis.FinanzGruppeZahlungDTO(
        z.id,
        z.datum,

        case
            when z.urspruenglicherZahlungsnachweis is not null
            then -z.betrag
            else z.betrag
        end,

        z.zahlungsweg,
        z.bemerkung,

        (select count(d)
         from Dokument d
         where d.zahlungsnachweis.id = z.id)
    )
    from Zahlungsnachweis z
    where z.veranstaltung.id = :veranstaltungId
      and z.finanzGruppe.id = :finanzGruppeId
      and z.zahlungsweg =
          com.kcserver.enumtype.Zahlungsweg.QUITTUNG
    order by z.datum desc, z.id desc
""")
    List<FinanzGruppeZahlungDTO> findZahlungenByFinanzGruppe(
            @Param("veranstaltungId") Long veranstaltungId,
            @Param("finanzGruppeId") Long finanzGruppeId
    );

    @Query("""
    select distinct new com.kcserver.dto.zahlungsnachweis.FinanzGruppeZahlungDTO(
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
    left join z.positionen p
    where z.veranstaltung.id = :veranstaltungId
      and z.urspruenglicherZahlungsnachweis is null
      and z.zahlungsweg =
          com.kcserver.enumtype.Zahlungsweg.UEBERWEISUNG
      and (
          p.teilnehmer.finanzGruppe.id = :finanzGruppeId
          or z.ueberzahlungsFinanzGruppe.id = :finanzGruppeId
      )
    order by z.datum desc, z.id desc
""")
    List<FinanzGruppeZahlungDTO> findUrspruenglicheUeberweisungenByFinanzGruppe(
            @Param("veranstaltungId") Long veranstaltungId,
            @Param("finanzGruppeId") Long finanzGruppeId
    );


    @Query("""
    select new com.kcserver.dto.zahlungsnachweis.FinanzGruppeZahlungDTO(
        z.id,
        z.datum,

        -z.betrag,

        z.zahlungsweg,
        z.bemerkung,

        (select count(d)
         from Dokument d
         where d.zahlungsnachweis.id = z.id)
    )
    from Zahlungsnachweis z
    where z.veranstaltung.id = :veranstaltungId
      and z.urspruenglicherZahlungsnachweis is not null
      and z.zahlungsweg =
          com.kcserver.enumtype.Zahlungsweg.UEBERWEISUNG
      and z.finanzGruppe.id = :finanzGruppeId
    order by z.datum desc, z.id desc
""")
    List<FinanzGruppeZahlungDTO> findUeberweisungsRueckzahlungenByFinanzGruppe(
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
      and z.urspruenglicherZahlungsnachweis is null
      and z.zahlungsweg = :zahlungsweg
""")
    BigDecimal sumBetragByVeranstaltungAndZahlungsweg(
            @Param("veranstaltungId") Long veranstaltungId,
            @Param("zahlungsweg") Zahlungsweg zahlungsweg
    );

    boolean existsByVeranstaltungId(Long veranstaltungId);

    @Query("""
    select coalesce(sum(z.betrag), 0)
    from Zahlungsnachweis z
    where z.veranstaltung.id = :veranstaltungId
      and z.urspruenglicherZahlungsnachweis is null
""")
    BigDecimal sumBetragByVeranstaltung(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
    select new com.kcserver.dto.zahlungsnachweis.OffeneUeberzahlungDTO(
        z.id,
        z.datum,
        z.betrag,

        coalesce(
            (
                select sum(p.betrag)
                from ZahlungsPosition p
                where p.zahlungsnachweis.id = z.id
            ),
            0
        ),

        coalesce(
            (
                select sum(r.betrag)
                from Zahlungsnachweis r
                where r.urspruenglicherZahlungsnachweis.id = z.id
            ),
            0
        ),

        (
            z.betrag
            - coalesce(
                (
                    select sum(p.betrag)
                    from ZahlungsPosition p
                    where p.zahlungsnachweis.id = z.id
                ),
                0
            )
            - coalesce(
                (
                    select sum(r.betrag)
                    from Zahlungsnachweis r
                    where r.urspruenglicherZahlungsnachweis.id = z.id
                ),
                0
            )
        ),

        z.bemerkung,

        ueberzahlungsFinanzGruppe.id,
        ueberzahlungsFinanzGruppe.kuerzel
    )
    from Zahlungsnachweis z
    left join z.ueberzahlungsFinanzGruppe ueberzahlungsFinanzGruppe
    where z.veranstaltung.id = :veranstaltungId
      and z.urspruenglicherZahlungsnachweis IS NULL
      and (
            z.betrag
            - coalesce(
                (
                    select sum(p.betrag)
                from ZahlungsPosition p
                where p.zahlungsnachweis.id = z.id
                ),
                0
            )
            - coalesce(
                (
                    select sum(r.betrag)
                    from Zahlungsnachweis r
                    where r.urspruenglicherZahlungsnachweis.id = z.id
                ),
                0
            )
        ) > 0
    order by z.datum desc, z.id desc
""")
    List<OffeneUeberzahlungDTO> findOffeneUeberzahlungen(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
        select coalesce(sum(p.betrag), 0)
        from ZahlungsPosition p
        where p.zahlungsnachweis.veranstaltung.id = :veranstaltungId
        """)
    BigDecimal sumZugeordneteTeilnehmerbeitraege(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
    select coalesce(sum(z.betrag), 0)
    from Zahlungsnachweis z
    where z.veranstaltung.id = :veranstaltungId
      and z.urspruenglicherZahlungsnachweis is null
      and z.zahlungsweg =
          com.kcserver.enumtype.Zahlungsweg.UEBERWEISUNG
""")
    BigDecimal sumUeberweisungenByVeranstaltung(
            @Param("veranstaltungId")
            Long veranstaltungId
    );

    @Query("""
    SELECT COALESCE(SUM(z.betrag), 0)
    FROM Zahlungsnachweis z
    WHERE z.urspruenglicherZahlungsnachweis.id = :zahlungsnachweisId
""")
    BigDecimal sumRueckzahlungenByUrspruenglichemZahlungsnachweisId(
            @Param("zahlungsnachweisId") Long zahlungsnachweisId
    );

    @Query("""
    select
        r.urspruenglicherZahlungsnachweis.id as zahlungsnachweisId,
        coalesce(sum(r.betrag), 0) as zurueckgezahlt
    from Zahlungsnachweis r
    where r.veranstaltung.id = :veranstaltungId
      and r.urspruenglicherZahlungsnachweis is not null
    group by r.urspruenglicherZahlungsnachweis.id
""")
    List<ZahlungsnachweisRueckzahlungSumme>
    sumRueckzahlungenByVeranstaltungGrouped(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
    select coalesce(sum(z.betrag), 0)
    from Zahlungsnachweis z
    where z.veranstaltung.id = :veranstaltungId
      and z.urspruenglicherZahlungsnachweis is not null
""")
    BigDecimal sumRueckzahlungenByVeranstaltung(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
    select coalesce(sum(z.betrag), 0)
    from Zahlungsnachweis z
    where z.veranstaltung.id = :veranstaltungId
      and z.urspruenglicherZahlungsnachweis is not null
      and z.zahlungsweg = :zahlungsweg
""")
    BigDecimal sumRueckzahlungenByVeranstaltungAndZahlungsweg(
            @Param("veranstaltungId") Long veranstaltungId,
            @Param("zahlungsweg") Zahlungsweg zahlungsweg
    );

    @Query("""
    select
        z.finanzGruppe.id,
        coalesce(sum(z.betrag), 0)
    from Zahlungsnachweis z
    where z.veranstaltung.id = :veranstaltungId
      and z.urspruenglicherZahlungsnachweis is not null
      and z.zahlungsweg =
          com.kcserver.enumtype.Zahlungsweg.QUITTUNG
      and z.finanzGruppe is not null
    group by z.finanzGruppe.id
""")
    List<Object[]> sumRueckzahlungenQuittungByFinanzGruppeGrouped(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
    select
        z.finanzGruppe.id,
        coalesce(sum(z.betrag), 0)
    from Zahlungsnachweis z
    where z.veranstaltung.id = :veranstaltungId
      and z.urspruenglicherZahlungsnachweis is not null
      and z.zahlungsweg =
          com.kcserver.enumtype.Zahlungsweg.UEBERWEISUNG
      and z.finanzGruppe is not null
    group by z.finanzGruppe.id
""")
    List<Object[]> sumRueckzahlungenUeberweisungByFinanzGruppeGrouped(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
    select
        z.finanzGruppe.id,
        coalesce(sum(z.betrag), 0)
    from Zahlungsnachweis z
    where z.veranstaltung.id = :veranstaltungId
      and z.urspruenglicherZahlungsnachweis is not null
    group by z.finanzGruppe.id
""")
    List<Object[]> sumRueckzahlungenByFinanzGruppeGrouped(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @EntityGraph(attributePaths = {
            "positionen",
            "positionen.teilnehmer",
            "positionen.teilnehmer.person",
            "positionen.teilnehmer.finanzGruppe",
            "finanzGruppe",
            "ueberzahlungsFinanzGruppe",
            "urspruenglicherZahlungsnachweis"
    })
    @Query("""
    select distinct z
    from Zahlungsnachweis z
    where z.veranstaltung.id = :veranstaltungId
    order by z.datum asc, z.id asc
""")
    List<Zahlungsnachweis> findDetailsByVeranstaltungId(
            @Param("veranstaltungId") Long veranstaltungId
    );

}