package com.kcserver.vermietung.repository;
import com.kcserver.vermietung.entity.VermietungAbrechnung;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface VermietungAbrechnungRepository extends JpaRepository<VermietungAbrechnung,Long> {
 @org.springframework.data.jpa.repository.Query("select e from VermietungAbrechnung e where e.buchung.id=:id and e.abrechnungsJahr is null order by e.revision desc")
 List<VermietungAbrechnung> findByBuchungIdOrderByRevisionDesc(@org.springframework.data.repository.query.Param("id") Long id);
 List<VermietungAbrechnung> findByBuchungIdAndAbrechnungsJahrIsNotNullOrderByAbrechnungsJahrDesc(Long id);
 Optional<VermietungAbrechnung> findByBuchungIdAndAbrechnungsJahr(Long id,Integer jahr);
 Optional<VermietungAbrechnung> findByIdAndBuchungId(Long id,Long buchungId);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from VermietungAbrechnung e where e.buchung.id = :id")
    void deleteAllByBuchungId(@org.springframework.data.repository.query.Param("id") Long id);
    @org.springframework.data.jpa.repository.Modifying(flushAutomatically=true)
    @org.springframework.data.jpa.repository.Query("update VermietungAbrechnung e set e.pdf=:leer where e.buchung.id=:buchung and e.id<>:aktuell and e.abrechnungsJahr is null")
    void entferneFrueherePdfs(@org.springframework.data.repository.query.Param("buchung") Long buchung,
        @org.springframework.data.repository.query.Param("aktuell") Long aktuell,
        @org.springframework.data.repository.query.Param("leer") byte[] leer);
    long countByBuchungId(Long id);
}
