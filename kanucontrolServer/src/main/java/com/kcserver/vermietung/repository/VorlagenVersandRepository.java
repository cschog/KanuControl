package com.kcserver.vermietung.repository;
import com.kcserver.vermietung.entity.VorlagenVersand;
public interface VorlagenVersandRepository extends org.springframework.data.jpa.repository.JpaRepository<VorlagenVersand,String> {
    java.util.List<VorlagenVersand> findByBuchungIdOrderByErstelltAmDesc(Long buchung);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from VorlagenVersand e where e.buchungId = :id")
    void deleteAllByBuchungId(@org.springframework.data.repository.query.Param("id") Long id);
    long countByBuchungIdAndVorgang(Long id, String vorgang);
}
