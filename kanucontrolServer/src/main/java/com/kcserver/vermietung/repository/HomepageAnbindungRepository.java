package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.HomepageAnbindung;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HomepageAnbindungRepository extends JpaRepository<HomepageAnbindung, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select h from HomepageAnbindung h where h.mietobjektId = :id")
    java.util.Optional<HomepageAnbindung> lockById(@org.springframework.data.repository.query.Param("id") Long id);
}
