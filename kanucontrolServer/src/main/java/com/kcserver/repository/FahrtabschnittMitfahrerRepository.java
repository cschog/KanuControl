package com.kcserver.repository;

import com.kcserver.entity.FahrtabschnittMitfahrer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FahrtabschnittMitfahrerRepository
        extends JpaRepository<FahrtabschnittMitfahrer, Long> {

    boolean existsByPersonId(Long personId);

    boolean existsByFahrtabschnittAbrechnungIdAndPersonId(
            Long abrechnungId,
            Long personId
    );
}
