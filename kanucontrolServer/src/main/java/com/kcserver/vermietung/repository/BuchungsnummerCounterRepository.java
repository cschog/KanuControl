package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.BuchungsnummerCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface BuchungsnummerCounterRepository
        extends JpaRepository<BuchungsnummerCounter, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BuchungsnummerCounter> findByJahr(Integer jahr);
}