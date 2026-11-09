package com.kcserver.vermietung.repository;
import com.kcserver.vermietung.entity.MietobjektVorlage;
import com.kcserver.vermietung.service.VorlagenModell.Vorgang;
public interface MietobjektVorlageRepository extends org.springframework.data.jpa.repository.JpaRepository<MietobjektVorlage,Long> {
    java.util.Optional<MietobjektVorlage> findByMietobjektIdAndVorgang(Long objekt, Vorgang vorgang);
}
