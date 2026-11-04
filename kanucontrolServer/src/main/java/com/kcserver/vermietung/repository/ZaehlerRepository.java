package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.Zaehler;
import com.kcserver.vermietung.enumtype.Verbrauchsart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZaehlerRepository extends JpaRepository<Zaehler, Long> {

    @org.springframework.data.jpa.repository.Query("""
    select distinct z from Zaehler z
    left join fetch z.mietbereiche mb left join fetch mb.mietobjekt
    where z.id in (select z2.id from Mietbereich m join m.zaehler z2 where m.mietobjekt.id=:objektId)
    order by z.verbrauchsart,z.id
    """)
    List<Zaehler> findForConsumptionDashboard(@org.springframework.data.repository.query.Param("objektId") Long objektId);

    List<Zaehler> findAllByOrderByIdAsc();

    List<Zaehler> findByVerbrauchsartAndAktivTrue(
            Verbrauchsart verbrauchsart
    );
}