package com.kcserver.repository.vermietung;

import com.kcserver.entity.vermietung.Mietobjekt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MietobjektRepository
        extends JpaRepository<Mietobjekt, Long> {

    List<Mietobjekt> findByVereinId(Long vereinId);

    Optional<Mietobjekt> findByAktivTrue();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update Mietobjekt m
       set m.aktiv = false
     where m.aktiv = true
""")
    int unsetAktivesMietobjekt();

    Optional<Mietobjekt> findTopByIdNotOrderByIdDesc(Long id);

}