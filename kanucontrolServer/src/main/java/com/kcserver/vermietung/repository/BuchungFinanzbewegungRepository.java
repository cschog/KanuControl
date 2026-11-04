package com.kcserver.vermietung.repository;
import com.kcserver.vermietung.entity.BuchungFinanzbewegung;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface BuchungFinanzbewegungRepository extends JpaRepository<BuchungFinanzbewegung,Long> {
 @org.springframework.data.jpa.repository.Query("""
 select m from BuchungFinanzbewegung m
 join fetch m.buchung b join fetch b.mieter join fetch b.mietobjekt
 where m.datum <= :stand
 """)
 List<BuchungFinanzbewegung> findForDashboard(@org.springframework.data.repository.query.Param("stand") java.time.LocalDate stand);
 List<BuchungFinanzbewegung> findByBuchungIdOrderByDatumAscIdAsc(Long buchungId);
}
