package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.MietbereichRabatt;
import com.kcserver.vermietung.enumtype.Rabattart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MietbereichRabattRepository
        extends JpaRepository<MietbereichRabatt, Long> {

    List<MietbereichRabatt> findByMietbereichIdOrderByIdAsc(
            Long mietbereichId
    );

    Optional<MietbereichRabatt> findByIdAndMietbereichId(
            Long id,
            Long mietbereichId
    );

    boolean existsByMietbereichIdAndRabattart(
            Long mietbereichId,
            Rabattart rabattart
    );

    boolean existsByMietbereichIdAndRabattartAndIdNot(
            Long mietbereichId,
            Rabattart rabattart,
            Long id
    );
}