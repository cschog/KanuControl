package com.kcserver.fahrten.repository;

import com.kcserver.fahrten.entity.FahrtKanu;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.List;
import java.util.Optional;

public interface FahrtKanuRepository extends JpaRepository<FahrtKanu, Long> {
    List<FahrtKanu> findByFahrt_IdOrderById(Long fahrtId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<FahrtKanu> findByIdAndFahrt_Id(Long id, Long fahrtId);
    boolean existsByKanu_Id(Long kanuId);
}
