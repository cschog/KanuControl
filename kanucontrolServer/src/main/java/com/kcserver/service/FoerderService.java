package com.kcserver.service;

import com.kcserver.config.FoerderConfig;
import com.kcserver.dto.foerder.FoerdersatzLookupResult;
import com.kcserver.entity.*;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.dto.simulation.PlanungsSimulation;
import com.kcserver.service.veranstaltung.VeranstaltungBerechnungsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FoerderService {

    private final FoerdersatzService foerdersatzService;
    private final KikZuschlagService kikZuschlagService;
    private final AltersService altersService;

    private final VeranstaltungBerechnungsService veranstaltungBerechnungsService;

    private static final int MAX_FOERDERTAGE_FM_JEM = 21;

    private boolean isFmJem(VeranstaltungTyp typ) {

        return typ == VeranstaltungTyp.FM
                || typ == VeranstaltungTyp.JEM;
    }

    public boolean istFoerderfaehig(

            Veranstaltung veranstaltung,
            Teilnehmer teilnehmer
    ) {
        if (veranstaltung == null || teilnehmer == null) {
            return false;
        }

        VeranstaltungTyp typ = veranstaltung.getTyp();

        if (typ == null || !typ.isFoerderfaehig()) {
            return false;
        }

        // Mitarbeiter/Leiter nicht förderfähig
        if (teilnehmer.getRolle() != null) {
            return false;
        }

        LocalDate geburt =
                teilnehmer.getPerson() != null
                        ? teilnehmer.getPerson().getGeburtsdatum()
                        : null;

        Integer alter =
                altersService.berechneAlterBeiBeginn(
                        geburt,
                        veranstaltung.getBeginnDatum()
                );

        if (alter == null) {
            return false;
        }

        return alter >= typ.getMindestalter()
                && alter <= typ.getHoechstalter();
    }

    public long countFoerderfaehigeTeilnehmer(

            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer
    ) {
        return teilnehmer.stream()
                .filter(t ->
                        istFoerderfaehig(
                                veranstaltung,
                                t
                        )
                )
                .count();
    }

    public int berechneFoerdertage(
            Veranstaltung veranstaltung
    ) {

        if (veranstaltung == null) {
            return 0;
        }

        int tage = (int) veranstaltungBerechnungsService
                .ermittleTage(veranstaltung);

        if (isFmJem(veranstaltung.getTyp())) {
            return Math.min(tage, MAX_FOERDERTAGE_FM_JEM);
        }

        return tage;
    }

    public int berechneFoerdertage(
            Planung planung
    ) {

        if (planung == null) {
            return 0;
        }

        Veranstaltung veranstaltung = planung.getVeranstaltung();

        if (veranstaltung == null) {
            return 0;
        }

        int tage = (int) veranstaltungBerechnungsService
                .ermittleTage(veranstaltung);

        if (isFmJem(veranstaltung.getTyp())) {
            return Math.min(tage, MAX_FOERDERTAGE_FM_JEM);
        }

        return tage;
    }

    public int berechneFoerdertage(
            PlanungsSimulation simulation
    ) {

        if (simulation == null) {
            return 0;
        }

        if (isFmJem(simulation.getVeranstaltung().getTyp())) {
            return (int) Math.min(
                    simulation.getVeranstaltung().getTage(),
                    MAX_FOERDERTAGE_FM_JEM
            );
        }

        return (int) simulation.getVeranstaltung().getTage();
    }

    /**
     * Förderung PRO TAG für einen Teilnehmer.
     */
    public BigDecimal berechneFoerderungProTagUndTeilnehmer(
            Veranstaltung veranstaltung,
            Teilnehmer teilnehmer
    ) {

        if (veranstaltung == null) {
            return BigDecimal.ZERO;
        }

        if (!istFoerderfaehig(veranstaltung, teilnehmer)) {
            return BigDecimal.ZERO;
        }

        return berechneAngewandtenFoerdersatz(
                veranstaltung
        );
    }

    public BigDecimal berechneKjfpZuschuss(

            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer
    ) {

        if (veranstaltung == null || teilnehmer == null) {
            return BigDecimal.ZERO;
        }

        int tage =
                berechneFoerdertage(veranstaltung);

        return teilnehmer.stream()

                .filter(t ->
                        istFoerderfaehig(
                                veranstaltung,
                                t
                        )
                )

                .map(t ->
                        berechneFoerderungProTagUndTeilnehmer(
                                veranstaltung,
                                t
                        )
                )

                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(tage));
    }

    public BigDecimal berechneAngewandtenFoerdersatz(
            Veranstaltung veranstaltung
    ) {

        if (veranstaltung == null) {
            return BigDecimal.ZERO;
        }

        return ermittleTagessatz(
                veranstaltung.getTyp(),
                veranstaltung.getBeginnDatum(),
                veranstaltung.getVerein() != null
                        && veranstaltung.getVerein()
                        .isKikZertifiziertAm(
                                veranstaltung.getBeginnDatum()
                        )
        );
    }

    public BigDecimal berechneAngewandtenFoerdersatz(
            Planung planung
    ) {

        if (planung == null || planung.getVeranstaltung() == null) {
            return BigDecimal.ZERO;
        }

        return ermittleTagessatz(
                planung.getVeranstaltung().getTyp(),
                planung.getVeranstaltung().getBeginnDatum(),
                planung.isKikZertifiziert()
        );
    }

    public BigDecimal berechneAngewandtenFoerdersatz(
            PlanungsSimulation simulation
    ) {

        if (simulation == null) {
            return BigDecimal.ZERO;
        }

        return ermittleTagessatz(
                simulation.getVeranstaltung().getTyp(),
                simulation.getVeranstaltung().getBeginnDatum(),
                simulation.isKikZertifiziert()
        );
    }

    public BigDecimal berechneGeplanteFoerderung(
            PlanungsSimulation simulation
    ) {

        if (simulation == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal tagessatz =
                berechneAngewandtenFoerdersatz(simulation);

        return tagessatz
                .multiply(BigDecimal.valueOf(simulation.getTeilnehmer()))
                .multiply(
                        BigDecimal.valueOf(
                                berechneFoerdertage(simulation)
                        )
                );
    }

    public BigDecimal berechneGeplanteFoerderung(
            Planung planung
    ) {

        if (planung == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal tagessatz =
                berechneAngewandtenFoerdersatz(planung);

        return tagessatz
                .multiply(BigDecimal.valueOf(planung.getTeilnehmer()))
                .multiply(
                        BigDecimal.valueOf(
                                berechneFoerdertage(planung)
                        )
                );
    }

    private BigDecimal ermittleTagessatz(
            VeranstaltungTyp typ,
            LocalDate datum,
            boolean kikZertifiziert
    ) {

        if (typ == null
                || datum == null
                || !typ.isFoerderfaehig()) {
            return BigDecimal.ZERO;
        }

        FoerdersatzLookupResult result =
                foerdersatzService.getGueltigOderLetztenMitInfo(
                        typ,
                        datum
                );

        if (result == null
                || result.foerdersatz() == null
                || result.foerdersatz().getFoerdersatz() == null) {
            return BigDecimal.ZERO;
        }

        return result.foerdersatz()
                .getFoerdersatz()
                .add(
                        ermittleKikZuschlag(
                                datum,
                                kikZertifiziert
                        )
                )
                .min(FoerderConfig.FOERDERDECKEL);
    }

    private BigDecimal ermittleKikZuschlag(
            LocalDate datum,
            boolean kikZertifiziert
    ) {

        if (!kikZertifiziert) {
            return BigDecimal.ZERO;
        }

        KikZuschlag kik =
                kikZuschlagService.findOptionalOderLetztenGueltigen(datum);

        if (kik == null || kik.getKikZuschlag() == null) {
            return BigDecimal.ZERO;
        }

        return kik.getKikZuschlag();
    }
}