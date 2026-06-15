package com.kcserver.repository;

import com.kcserver.entity.AbrechnungBuchung;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.data.jpa.repository.Query;

public interface AbrechnungBuchungRepository
        extends JpaRepository<AbrechnungBuchung, Long> {

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
}