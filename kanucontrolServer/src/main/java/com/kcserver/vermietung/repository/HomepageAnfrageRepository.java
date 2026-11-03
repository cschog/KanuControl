package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.HomepageAnfrage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HomepageAnfrageRepository extends JpaRepository<HomepageAnfrage, Long> {
    Optional<HomepageAnfrage> findByMietobjektIdAndAnfrageId(Long mietobjektId, String anfrageId);
}
