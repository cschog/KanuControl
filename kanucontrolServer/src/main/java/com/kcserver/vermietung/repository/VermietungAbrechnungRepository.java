package com.kcserver.vermietung.repository;
import com.kcserver.vermietung.entity.VermietungAbrechnung;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface VermietungAbrechnungRepository extends JpaRepository<VermietungAbrechnung,Long> {
 List<VermietungAbrechnung> findByBuchungIdOrderByRevisionDesc(Long id);
 Optional<VermietungAbrechnung> findByIdAndBuchungId(Long id,Long buchungId);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from VermietungAbrechnung e where e.buchung.id = :id")
    void deleteAllByBuchungId(@org.springframework.data.repository.query.Param("id") Long id);
    long countByBuchungId(Long id);
}
