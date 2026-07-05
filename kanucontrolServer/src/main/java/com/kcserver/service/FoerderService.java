package com.kcserver.service;

import com.kcserver.config.FoerderConfig;
import com.kcserver.dto.foerder.FoerdersatzLookupResult;
import com.kcserver.entity.*;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.simulation.PlanungsSimulation;
import com.kcserver.simulation.PlanungsSimulationFactory;
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
    private final PlanungsSimulationFactory simulationFactory;

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

        return berechneFoerdertage(
                simulationFactory.fromVeranstaltung(
                        veranstaltung
                )
        );
    }

    public int berechneFoerdertage(
            PlanungsSimulation simulation
    ) {

        if (simulation == null) {
            return 0;
        }

        if (isFmJem(simulation.getTyp())) {
            return (int) Math.min(
                    simulation.getTage(),
                    MAX_FOERDERTAGE_FM_JEM
            );
        }

        return (int) simulation.getTage();
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

    public BigDecimal berechneGesamtfoerderung(

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
            PlanungsSimulation simulation
    ) {

        if (simulation == null) {
            return BigDecimal.ZERO;
        }

        return ermittleTagessatz(
                simulation.getTyp(),
                simulation.getBeginnDatum(),
                simulation.isKikZertifiziert()
        );
    }

    public BigDecimal berechneGeplanteFoerderung(
            Veranstaltung veranstaltung
    ) {

        if (veranstaltung == null) {
            return BigDecimal.ZERO;
        }

        return berechneGeplanteFoerderung(
                simulationFactory.fromVeranstaltung(
                        veranstaltung
                )
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
                kikZuschlagService.findOptionalGueltigAm(datum);

        if (kik == null || kik.getKikZuschlag() == null) {
            return BigDecimal.ZERO;
        }

        return kik.getKikZuschlag();
    }
}