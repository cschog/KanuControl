package com.kcserver.vermietung.repository;
import com.kcserver.vermietung.entity.VorlagenVersand;
public interface VorlagenVersandRepository extends org.springframework.data.jpa.repository.JpaRepository<VorlagenVersand,String> {
    java.util.List<VorlagenVersand> findByBuchungIdOrderByErstelltAmDesc(Long buchung);
}
