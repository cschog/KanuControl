package com.kcserver.kjfp.repository.beitrag;

import com.kcserver.kjfp.entity.beitraege.Beitragsregel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeitragsregelRepository
        extends JpaRepository<Beitragsregel, Long> {
}