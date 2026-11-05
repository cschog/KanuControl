package com.kcserver.fahrten.repository;

import com.kcserver.fahrten.entity.Fahrt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FahrtRepository extends JpaRepository<Fahrt, Long> {
    List<Fahrt> findByVeranstaltung_IdOrderByDatumAscBeginnZeitAsc(Long veranstaltungId);
    Optional<Fahrt> findByIdAndVeranstaltung_Id(Long id, Long veranstaltungId);
}
