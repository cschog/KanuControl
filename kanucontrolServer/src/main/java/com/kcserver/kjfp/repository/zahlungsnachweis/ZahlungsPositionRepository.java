package com.kcserver.kjfp.repository.zahlungsnachweis;

import com.kcserver.kjfp.entity.beitraege.ZahlungsPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface ZahlungsPositionRepository
        extends JpaRepository<ZahlungsPosition, Long> {

    List<ZahlungsPosition> findByZahlungsnachweisIdOrderByIdAsc(
            Long zahlungsnachweisId
    );

    List<ZahlungsPosition> findByTeilnehmerIdOrderByIdAsc(
            Long teilnehmerId
    );

    List<ZahlungsPosition> findByTeilnehmerVeranstaltungIdOrderByIdAsc(
            Long veranstaltungId
    );

    @Query("""
    select coalesce(sum(z.betrag), 0)
    from ZahlungsPosition z
    where z.teilnehmer.id = :teilnehmerId
""")
    BigDecimal sumBetragByTeilnehmerId(
            @Param("teilnehmerId") Long teilnehmerId
    );

    @Query("""
    select coalesce(sum(z.betrag), 0)
    from ZahlungsPosition z
    where z.teilnehmer.veranstaltung.id = :veranstaltungId
""")
    BigDecimal sumBetragByVeranstaltungId(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
    select coalesce(sum(case when n.urspruenglicherZahlungsnachweis is not null then -p.betrag else p.betrag end), 0)
    from ZahlungsPosition p join p.zahlungsnachweis n
    where n.veranstaltung.id = :veranstaltungId
      and p.teilnehmer.kjfpZugeordnet = true
    """)
    BigDecimal sumKjfpBetragByVeranstaltung(@Param("veranstaltungId") Long veranstaltungId);
}
