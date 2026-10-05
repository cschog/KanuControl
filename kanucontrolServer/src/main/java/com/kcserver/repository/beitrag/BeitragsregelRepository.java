package com.kcserver.repository.beitrag;

import com.kcserver.entity.beitraege.Beitragsregel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeitragsregelRepository
        extends JpaRepository<Beitragsregel, Long> {
}