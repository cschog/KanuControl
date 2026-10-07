package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.MietbereichEigenschaft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MietbereichEigenschaftRepository
        extends JpaRepository<MietbereichEigenschaft, Long> {

    List<MietbereichEigenschaft> findByMietbereichIdOrderBySortierungAscIdAsc(Long mietbereichId);

}