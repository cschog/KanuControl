package com.kcserver.fahrten.repository;

import com.kcserver.fahrten.entity.Gewaesser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GewaesserRepository extends JpaRepository<Gewaesser, Long> {
    List<Gewaesser> findAllByOrderByName();
    boolean existsByNameIgnoreCase(String name);
}
