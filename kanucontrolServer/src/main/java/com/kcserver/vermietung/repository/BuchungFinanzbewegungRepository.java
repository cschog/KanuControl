package com.kcserver.vermietung.repository;
import com.kcserver.vermietung.entity.BuchungFinanzbewegung;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface BuchungFinanzbewegungRepository extends JpaRepository<BuchungFinanzbewegung,Long> {
 List<BuchungFinanzbewegung> findByBuchungIdOrderByDatumAscIdAsc(Long buchungId);
}
