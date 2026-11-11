package com.kcserver.vermietung.repository;
import com.kcserver.vermietung.entity.VorlagenImport;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Optional;
public interface VorlagenImportRepository extends JpaRepository<VorlagenImport,String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from VorlagenImport i where i.id=:id and i.benutzer=:benutzer and i.tenant=:tenant")
    Optional<VorlagenImport> gesperrt(@Param("id") String id,@Param("benutzer") String benutzer,@Param("tenant") String tenant);
    @Modifying @Query("delete from VorlagenImport i where i.ablauf < :jetzt")
    void abgelaufeneEntfernen(@Param("jetzt") Instant jetzt);
    long countByBenutzerAndTenantAndUebernommenFalse(String benutzer,String tenant);
}
