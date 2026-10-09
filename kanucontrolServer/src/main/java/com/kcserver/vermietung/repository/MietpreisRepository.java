package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.Mietpreis;
import com.kcserver.vermietung.enumtype.Buchungsquelle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MietpreisRepository extends JpaRepository<Mietpreis, Long> {

    List<Mietpreis> findByMietbereichIdOrderByGueltigAbDesc(Long mietbereichId);

    Optional<Mietpreis>
    findFirstByMietbereichIdAndBuchungsquelleAndGueltigAbLessThanEqualOrderByGueltigAbDesc(
            Long mietbereichId,
            Buchungsquelle buchungsquelle,
            LocalDate stichtag
    );

    boolean existsByMietbereichIdAndBuchungsquelleAndGueltigAbAndIdNot(
            Long mietbereichId,
            Buchungsquelle buchungsquelle,
            LocalDate gueltigAb,
            Long id
    );

    boolean existsByMietbereichIdAndBuchungsquelleAndGueltigAb(
            Long mietbereichId,
            Buchungsquelle buchungsquelle,
            LocalDate gueltigAb
    );
}