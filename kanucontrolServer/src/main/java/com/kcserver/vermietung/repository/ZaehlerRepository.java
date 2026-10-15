package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.Zaehler;
import com.kcserver.vermietung.enumtype.Verbrauchsart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZaehlerRepository extends JpaRepository<Zaehler, Long> {

    List<Zaehler> findByMietobjektIdOrderByIdAsc(Long mietobjektId);

    List<Zaehler> findByMietobjektIdAndVerbrauchsartAndAktivTrue(
            Long mietobjektId,
            Verbrauchsart verbrauchsart
    );
}