package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.Mieter;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MieterRepository extends JpaRepository<Mieter, Long> {
    @EntityGraph(attributePaths = "person")
    List<Mieter> findAllByOrderByPersonNameAscPersonVornameAsc();

    @EntityGraph(attributePaths = "person")
    Optional<Mieter> findById(Long id);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select m from Mieter m where m.id = :id")
    Optional<Mieter> findLockedById(@org.springframework.data.repository.query.Param("id") Long id);

    Optional<Mieter> findByPersonId(Long personId);
    boolean existsByPersonId(Long personId);
}
