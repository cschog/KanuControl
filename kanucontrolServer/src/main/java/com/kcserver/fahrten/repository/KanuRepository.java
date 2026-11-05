package com.kcserver.fahrten.repository;

import com.kcserver.fahrten.entity.Kanu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KanuRepository extends JpaRepository<Kanu, Long> {
    List<Kanu> findByVeranstaltungIsNullOrderByKennzeichnung();
    List<Kanu> findByVeranstaltung_IdOrderByKennzeichnung(Long veranstaltungId);
    boolean existsByVeranstaltungIsNullAndKennzeichnungIgnoreCase(String kennzeichnung);
}
