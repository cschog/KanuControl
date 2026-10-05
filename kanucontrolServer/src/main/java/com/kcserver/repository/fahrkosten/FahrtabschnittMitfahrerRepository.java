package com.kcserver.repository.fahrkosten;

import com.kcserver.entity.fahrkosten.FahrtabschnittMitfahrer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FahrtabschnittMitfahrerRepository
        extends JpaRepository<FahrtabschnittMitfahrer, Long> {

    boolean existsByPersonId(Long personId);

    boolean existsByFahrtabschnittAbrechnungIdAndPersonId(
            Long abrechnungId,
            Long personId
    );
}
