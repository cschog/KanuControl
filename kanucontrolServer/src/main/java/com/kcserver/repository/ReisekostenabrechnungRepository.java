package com.kcserver.repository;

import com.kcserver.entity.Reisekostenabrechnung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReisekostenabrechnungRepository
        extends JpaRepository<Reisekostenabrechnung, Long> {

    List<Reisekostenabrechnung> findByVeranstaltungId(
            Long veranstaltungId
    );
}