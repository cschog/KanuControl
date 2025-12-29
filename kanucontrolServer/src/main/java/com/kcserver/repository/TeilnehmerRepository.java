package com.kcserver.repository;

import com.kcserver.entity.*;
import com.kcserver.enumtype.TeilnehmerRolle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeilnehmerRepository extends JpaRepository<Teilnehmer, Long> {

    // Alle Teilnehmer einer Veranstaltung
    List<Teilnehmer> findByVeranstaltung(Veranstaltung veranstaltung);

    // Eindeutiger Teilnehmer pro Veranstaltung + Person
    Optional<Teilnehmer> findByVeranstaltungAndPerson(
            Veranstaltung veranstaltung,
            Person person
    );

    // Alle Teilnehmer einer Rolle (z.B. LEITER)
    Optional<Teilnehmer> findByVeranstaltungAndRolle(
            Veranstaltung veranstaltung,
            TeilnehmerRolle rolle
    );
}