package com.kcserver.repository;

import com.kcserver.entity.PlanungPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanungPositionRepository
        extends JpaRepository<PlanungPosition, Long> {

    List<PlanungPosition> findByPlanungId(Long planungId);
}