package com.kcserver.repository;

import com.kcserver.entity.Mitglied;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public
interface MitgliedRepository extends JpaRepository<Mitglied, Long> {
        List<Mitglied> findByHauptVerein(boolean hauptVerein);
}

