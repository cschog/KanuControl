package com.kcserver.vermietung.repository;
import com.kcserver.vermietung.entity.BuchungsnachrichtEntwurf;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface BuchungsnachrichtEntwurfRepository extends JpaRepository<BuchungsnachrichtEntwurf,String> {
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select e from BuchungsnachrichtEntwurf e where e.id=:id and e.benutzer=:benutzer and e.tenant=:tenant and e.buchungId=:buchungId")
 Optional<BuchungsnachrichtEntwurf> gesperrt(String id,String benutzer,String tenant,Long buchungId);
 List<BuchungsnachrichtEntwurf> findByBuchungIdAndBenutzerAndTenantOrderByAblaufDesc(Long buchungId,String benutzer,String tenant);
}
