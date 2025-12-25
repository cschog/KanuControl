package com.kcserver.repository;

import com.kcserver.entity.Mitglied;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface MitgliedRepository extends JpaRepository<Mitglied, Long> {

    List<Mitglied> findByPerson_Id(Long personId);

    List<Mitglied> findByVerein_Id(Long vereinId);

    List<Mitglied> findByPerson_IdAndHauptVereinTrue(Long personId);
}