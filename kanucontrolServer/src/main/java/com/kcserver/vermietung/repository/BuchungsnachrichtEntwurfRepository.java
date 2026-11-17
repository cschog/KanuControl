package com.kcserver.vermietung.repository;
import com.kcserver.vermietung.entity.BuchungsnachrichtEntwurf;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface BuchungsnachrichtEntwurfRepository extends JpaRepository<BuchungsnachrichtEntwurf,String> {
 List<BuchungsnachrichtEntwurf> findByBuchungIdAndTenantAndUnabhaengigTrueAndAktuelleFassungTrue(Long buchungId,String tenant);
 @Modifying(flushAutomatically=true)
 @Query("update BuchungsnachrichtEntwurf e set e.pdf=null,e.originalPdf=null where e.buchungId=:buchung and e.tenant=:tenant and e.ziel=:ziel and e.unabhaengig=false and e.ausgefuehrt=true and e.id<>:aktuell")
 void entferneFruehereVersandPdfs(@org.springframework.data.repository.query.Param("buchung") Long buchung,
   @org.springframework.data.repository.query.Param("tenant") String tenant,
   @org.springframework.data.repository.query.Param("ziel") com.kcserver.vermietung.enumtype.Buchungsstatus ziel,
   @org.springframework.data.repository.query.Param("aktuell") String aktuell);
 @Modifying(flushAutomatically=true)
 @Query("delete from BuchungsnachrichtEntwurf e where e.buchungId=:buchung and e.tenant=:tenant and e.ziel=:ziel and e.unabhaengig=true and e.aktuelleFassung=false and e.id<>:aktuell")
 void entferneFruehereDokumente(@org.springframework.data.repository.query.Param("buchung") Long buchung,
   @org.springframework.data.repository.query.Param("tenant") String tenant,
   @org.springframework.data.repository.query.Param("ziel") com.kcserver.vermietung.enumtype.Buchungsstatus ziel,
   @org.springframework.data.repository.query.Param("aktuell") String aktuell);
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select e from BuchungsnachrichtEntwurf e where e.id=:id and e.tenant=:tenant and e.buchungId=:buchungId and e.unabhaengig=true and (e.benutzer=:benutzer or e.aktuelleFassung=true)")
 Optional<BuchungsnachrichtEntwurf> dokument(String id,String benutzer,String tenant,Long buchungId);
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select e from BuchungsnachrichtEntwurf e where e.id=:id and e.benutzer=:benutzer and e.tenant=:tenant and e.buchungId=:buchungId")
 Optional<BuchungsnachrichtEntwurf> gesperrt(String id,String benutzer,String tenant,Long buchungId);
 List<BuchungsnachrichtEntwurf> findByBuchungIdAndBenutzerAndTenantOrderByAblaufDesc(Long buchungId,String benutzer,String tenant);
}
