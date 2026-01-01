package com.kcserver.service;

import com.kcserver.entity.Erhebungsbogen;
import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.Sex;
import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.repository.ErhebungsbogenRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class ErhebungsbogenServiceImpl implements ErhebungsbogenService {

    private final ErhebungsbogenRepository erhebungsbogenRepository;
    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;

    public ErhebungsbogenServiceImpl(
            ErhebungsbogenRepository erhebungsbogenRepository,
            VeranstaltungRepository veranstaltungRepository,
            TeilnehmerRepository teilnehmerRepository
    ) {
        this.erhebungsbogenRepository = erhebungsbogenRepository;
        this.veranstaltungRepository = veranstaltungRepository;
        this.teilnehmerRepository = teilnehmerRepository;
    }

    /* =========================================================
       GET / CREATE
       ========================================================= */

    @Override
    public Erhebungsbogen getOrCreate(Long veranstaltungId) {

        Veranstaltung veranstaltung = getVeranstaltung(veranstaltungId);

        return erhebungsbogenRepository
                .findByVeranstaltung(veranstaltung)
                .orElseGet(() -> {
                    Erhebungsbogen bogen = new Erhebungsbogen();
                    bogen.setVeranstaltung(veranstaltung);
                    bogen.setAbgeschlossen(false);
                    bogen.setStichtag(veranstaltung.getBeginnDatum());
                    return erhebungsbogenRepository.save(bogen);
                });
    }

    /* =========================================================
       BERECHNUNG
       ========================================================= */

    @Override
    public Erhebungsbogen berechneStatistik(Long veranstaltungId) {

        Erhebungsbogen bogen = getOrCreate(veranstaltungId);

        if (bogen.isAbgeschlossen()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Erhebungsbogen ist bereits abgeschlossen"
            );
        }

        LocalDate stichtag = bogen.getStichtag();

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository.findByVeranstaltung(bogen.getVeranstaltung());

        // Reset (wichtig bei Neuberechnung!)
        resetTeilnehmerStatistik(bogen);

        for (Teilnehmer t : teilnehmer) {

            Person p = t.getPerson();
            long alter = ChronoUnit.YEARS.between(p.getGeburtsdatum(), stichtag);
            Sex sex = p.getSex();

            if (t.getRolle() == TeilnehmerRolle.TEILNEHMER) {
                zaehleTeilnehmer(bogen, sex, alter);
            }

            if (t.getRolle() == TeilnehmerRolle.MITARBEITER
                    || t.getRolle() == TeilnehmerRolle.LEITER) {
                zaehleMitarbeiter(bogen, sex);
            }
        }

        return erhebungsbogenRepository.save(bogen);
    }

    /* =========================================================
       ABSCHLUSS
       ========================================================= */

    @Override
    public Erhebungsbogen abschliessen(Long veranstaltungId) {

        Erhebungsbogen bogen = getOrCreate(veranstaltungId);

        if (bogen.isAbgeschlossen()) {
            return bogen;
        }

        berechneStatistik(veranstaltungId);
        bogen.setAbgeschlossen(true);

        return erhebungsbogenRepository.save(bogen);
    }

    /* =========================================================
       HELFER
       ========================================================= */
    private boolean istMitarbeiter(Teilnehmer t) {
        return t.getRolle() == TeilnehmerRolle.MITARBEITER
                || t.getRolle() == TeilnehmerRolle.LEITER;
    }

    private Veranstaltung getVeranstaltung(Long id) {
        return veranstaltungRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Veranstaltung not found"
                ));
    }

    private void resetTeilnehmerStatistik(Erhebungsbogen b) {
        b.setTeilnehmerMaennlichU6(0);
        b.setTeilnehmerMaennlich6_13(0);
        b.setTeilnehmerMaennlich14_17(0);
        b.setTeilnehmerMaennlich18Plus(0);

        b.setTeilnehmerWeiblichU6(0);
        b.setTeilnehmerWeiblich6_13(0);
        b.setTeilnehmerWeiblich14_17(0);
        b.setTeilnehmerWeiblich18Plus(0);

        b.setTeilnehmerDiversU6(0);
        b.setTeilnehmerDivers6_13(0);
        b.setTeilnehmerDivers14_17(0);
        b.setTeilnehmerDivers18Plus(0);

        b.setMitarbeiterMaennlich(0);
        b.setMitarbeiterWeiblich(0);
        b.setMitarbeiterDivers(0);
    }

    private void zaehleTeilnehmer(Erhebungsbogen b, Sex sex, long alter) {

        boolean u6 = alter < 6;
        boolean a6_13 = alter >= 6 && alter <= 13;
        boolean a14_17 = alter >= 14 && alter <= 17;
        boolean a18 = alter >= 18;

        switch (sex) {
            case MAENNLICH -> {
                if (u6) b.setTeilnehmerMaennlichU6(b.getTeilnehmerMaennlichU6() + 1);
                else if (a6_13) b.setTeilnehmerMaennlich6_13(b.getTeilnehmerMaennlich6_13() + 1);
                else if (a14_17) b.setTeilnehmerMaennlich14_17(b.getTeilnehmerMaennlich14_17() + 1);
                else if (a18) b.setTeilnehmerMaennlich18Plus(b.getTeilnehmerMaennlich18Plus() + 1);
            }
            case WEIBLICH -> {
                if (u6) b.setTeilnehmerWeiblichU6(b.getTeilnehmerWeiblichU6() + 1);
                else if (a6_13) b.setTeilnehmerWeiblich6_13(b.getTeilnehmerWeiblich6_13() + 1);
                else if (a14_17) b.setTeilnehmerWeiblich14_17(b.getTeilnehmerWeiblich14_17() + 1);
                else if (a18) b.setTeilnehmerWeiblich18Plus(b.getTeilnehmerWeiblich18Plus() + 1);
            }
            case DIVERS -> {
                if (u6) b.setTeilnehmerDiversU6(b.getTeilnehmerDiversU6() + 1);
                else if (a6_13) b.setTeilnehmerDivers6_13(b.getTeilnehmerDivers6_13() + 1);
                else if (a14_17) b.setTeilnehmerDivers14_17(b.getTeilnehmerDivers14_17() + 1);
                else if (a18) b.setTeilnehmerDivers18Plus(b.getTeilnehmerDivers18Plus() + 1);
            }
        }
    }

    private void zaehleMitarbeiter(Erhebungsbogen b, Sex sex) {
        switch (sex) {
            case MAENNLICH -> b.setMitarbeiterMaennlich(b.getMitarbeiterMaennlich() + 1);
            case WEIBLICH -> b.setMitarbeiterWeiblich(b.getMitarbeiterWeiblich() + 1);
            case DIVERS -> b.setMitarbeiterDivers(b.getMitarbeiterDivers() + 1);
        }
    }
}