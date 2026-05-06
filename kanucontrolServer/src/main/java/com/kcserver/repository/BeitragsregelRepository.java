package com.kcserver.repository;

import com.kcserver.entity.Beitragsregel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeitragsregelRepository
        extends JpaRepository<Beitragsregel, Long> {
}