package com.kcserver.repository;

import com.kcserver.entity.Verein;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VereinRepository extends JpaRepository<Verein, Long> {
    List<Verein> findByName(String name);
}
