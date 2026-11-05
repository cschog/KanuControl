package com.kcserver.fahrten.repository;

import com.kcserver.fahrten.entity.KanuLizenz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface KanuLizenzRepository extends JpaRepository<KanuLizenz, Long> {
    List<KanuLizenz> findByKanu_IdOrderByGueltigVonDesc(Long kanuId);
    boolean existsByKanu_IdAndGueltigVonLessThanEqualAndGueltigBisGreaterThanEqual(Long kanuId, LocalDate von, LocalDate bis);
}
