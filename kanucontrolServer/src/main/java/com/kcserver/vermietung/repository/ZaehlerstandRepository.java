package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.Zaehlerstand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ZaehlerstandRepository
        extends JpaRepository<Zaehlerstand, Long> {

    List<Zaehlerstand> findByZaehlerIdOrderByAblesedatumDesc(
            Long zaehlerId
    );

    List<Zaehlerstand> findByZaehlerIdInAndAblesedatumLessThanEqualOrderByAblesedatumAscIdAsc(
        java.util.Collection<Long> ids, LocalDateTime stand);

    boolean existsByZaehlerIdAndAblesedatumBetween(
            Long zaehlerId,
            LocalDateTime von,
            LocalDateTime bis
    );
}