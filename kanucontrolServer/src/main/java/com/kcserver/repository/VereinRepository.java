package com.kcserver.repository;

import com.kcserver.entity.Verein;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VereinRepository
        extends JpaRepository<Verein, Long>,
        JpaSpecificationExecutor<Verein> {

    boolean existsByAbkAndName(String abk, String name);

    List<Verein> findByName(String name);

    Verein findByNameIs(String name);

    List<Verein> findByNameStartingWith(String prefix);

    List<Verein> findByOrt(String ort);

    List<Verein> findByPlz(String plz);

    List<Verein> findByAbk(String abk);
}