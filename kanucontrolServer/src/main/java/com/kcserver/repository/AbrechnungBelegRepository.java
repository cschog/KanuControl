package com.kcserver.repository;

import com.kcserver.entity.AbrechnungBeleg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface AbrechnungBelegRepository
        extends JpaRepository<AbrechnungBeleg, Long> {

    boolean existsByFinanzGruppe_Id(Long gruppeId);

    long countByFinanzGruppe_Id(Long gruppeId);

    List<AbrechnungBeleg> findByAbrechnungId(Long abrechnungId);

    @Query("""
    SELECT b.finanzGruppe.id, COUNT(b.id)
    FROM AbrechnungBeleg b
    WHERE b.finanzGruppe.veranstaltung.id = :veranstaltungId
    GROUP BY b.finanzGruppe.id
""")
    List<Object[]> countByVeranstaltungGrouped(Long veranstaltungId);
}