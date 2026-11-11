package com.kcserver.vermietung.repository;

import com.kcserver.vermietung.entity.HomepageStatusMail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HomepageStatusMailRepository extends JpaRepository<HomepageStatusMail, String> {
    List<HomepageStatusMail> findTop20ByMietobjektIdAndQuittiertAmIsNullOrderByCreatedAtAscIdAsc(Long mietobjektId);
    Optional<HomepageStatusMail> findByIdAndMietobjektId(String id, Long mietobjektId);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from HomepageStatusMail e where e.buchungId = :id")
    void deleteAllByBuchungId(@org.springframework.data.repository.query.Param("id") Long id);
}
