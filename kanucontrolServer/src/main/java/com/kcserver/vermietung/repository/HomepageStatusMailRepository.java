package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.HomepageStatusMail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HomepageStatusMailRepository extends JpaRepository<HomepageStatusMail, String> {
    List<HomepageStatusMail> findTop20ByMietobjektIdAndQuittiertAmIsNullOrderByCreatedAtAscIdAsc(Long mietobjektId);
    Optional<HomepageStatusMail> findByIdAndMietobjektId(String id, Long mietobjektId);
}
