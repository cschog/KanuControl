package com.kcserver.repository;

import com.kcserver.entity.PlanungPosition;
import com.kcserver.enumtype.FinanzKategorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface PlanungPositionRepository
        extends JpaRepository<PlanungPosition, Long> {

    @Query("""
        select coalesce(sum(p.betrag), 0)
        from PlanungPosition p
        where p.planung.veranstaltung.id = :veranstaltungId
          and p.kategorie in :kategorien
    """)
    BigDecimal sumByKategorien(
            Long veranstaltungId,
            List<FinanzKategorie> kategorien
    );

    List<PlanungPosition>
    findByPlanung_Veranstaltung_Id(Long veranstaltungId);
}