package com.kcserver.repository;

import com.kcserver.entity.KikZuschlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface KikZuschlagRepository
        extends JpaRepository<KikZuschlag, Long> {

    @Query("""
        select k
        from KikZuschlag k
        where k.gueltigVon <= :datum
          and (k.gueltigBis is null or k.gueltigBis >= :datum)
        order by k.gueltigVon desc
    """)
    Optional<KikZuschlag> findGueltigAm(LocalDate datum);
}