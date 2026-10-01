package com.kcserver.service;

import com.kcserver.config.FoerderConfig;
import com.kcserver.entity.*;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.dto.simulation.PlanungsSimulation;
import com.kcserver.exception.ErrorMessages;
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

    private static final BigDecimal PLANUNG_FOERDERSATZ =
            BigDecimal.TEN;

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

        if (geburt == null) {
            return false;
        }

        Integer alterBeiBeginn =
                altersService.berechneAlter(
                        geburt,
                        veranstaltung.getBeginnDatum()
                );

        if (alterBeiBeginn == null) {
            return false;
        }

        /*
         * Höchstalter bleibt der Beginn der Veranstaltung.
         *
         * Wer zu Beginn noch im zulässigen Höchstalter ist,
         * bleibt für die gesamte Maßnahme förderfähig.
         */
        if (alterBeiBeginn > typ.getHoechstalter()) {
            return false;
        }

        /*
         * Mindestalter:
         *
         * Ein Teilnehmer darf das Mindestalter auch während
         * der Veranstaltung erreichen.
         *
         * Beispiel:
         * Mindestalter = 6
         * das Kind wird am letzten Veranstaltungstag 6 Jahre alt.
         * → förderfähig
         */
        LocalDate endeDatum =
                veranstaltung.getEndeDatum();

        if (endeDatum == null) {
            return alterBeiBeginn >= typ.getMindestalter();
        }

        Integer alterBeiEnde =
                altersService.berechneAlter(
                        geburt,
                        veranstaltung.getEndeDatum()
                );

        if (alterBeiEnde == null) {
            return false;
        }

        return alterBeiEnde >= typ.getMindestalter();
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
        if (simulation == null
                || simulation.getVeranstaltung() == null) {
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
     *
     * Ohne Fallback: Für die tatsächliche Förderung muss
     * ein gültiger offizieller Fördersatz vorhanden sein.
     */
    public BigDecimal berechneFoerderungProTagUndTeilnehmer(
            Veranstaltung veranstaltung,
            Teilnehmer teilnehmer
    ) {
        return berechneFoerderungProTagUndTeilnehmer(
                veranstaltung,
                teilnehmer,
                false
        );
    }


    /**
     * Förderung PRO TAG für einen Teilnehmer.
     *
     * fallbackAufPlanungssatz = true wird ausschließlich bei
     * der Abrechnung verwendet, wenn kein gültiger offizieller
     * Fördersatz vorhanden ist.
     */
    public BigDecimal berechneFoerderungProTagUndTeilnehmer(
            Veranstaltung veranstaltung,
            Teilnehmer teilnehmer,
            boolean fallbackAufPlanungssatz
    ) {

        if (veranstaltung == null) {
            return BigDecimal.ZERO;
        }

        if (!istFoerderfaehig(veranstaltung, teilnehmer)) {
            return BigDecimal.ZERO;
        }

        return berechneAngewandtenFoerdersatz(
                veranstaltung,
                fallbackAufPlanungssatz
        );
    }

    public BigDecimal berechneKjfpZuschuss(
            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer
    ) {
        return berechneKjfpZuschuss(
                veranstaltung,
                teilnehmer,
                false
        );
    }

    public BigDecimal berechneKjfpZuschuss(
            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer,
            boolean fallbackAufPlanungssatz
    ) {

        if (veranstaltung == null || teilnehmer == null) {
            return BigDecimal.ZERO;
        }

        int tage = berechneFoerdertage(veranstaltung);

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
                                t,
                                fallbackAufPlanungssatz
                        )
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(tage));
    }


    /**
     * Tatsächlicher Fördersatz einer Veranstaltung.
     *
     * Für die tatsächliche Förderung muss ein gültiger offizieller
     * Fördersatz für den Veranstaltungstag vorhanden sein.
     *
     * Kein Fallback auf 10 €!
     */
    public BigDecimal berechneAngewandtenFoerdersatz(
            Veranstaltung veranstaltung
    ) {
        if (veranstaltung == null) {
            return BigDecimal.ZERO;
        }

        VeranstaltungTyp typ = veranstaltung.getTyp();
        LocalDate datum = veranstaltung.getBeginnDatum();

        if (typ == null || datum == null || !typ.isFoerderfaehig()) {
            return BigDecimal.ZERO;
        }

        Foerdersatz foerdersatz =
                foerdersatzService.findOptionalGueltigFuerTypAm(
                        typ,
                        datum
                );

        if (foerdersatz == null
                || foerdersatz.getFoerdersatz() == null) {

            throw new IllegalStateException(
                    ErrorMessages.NO_VALID_FOERDERSATZ
            );
        }

        BigDecimal tagessatz =
                foerdersatz.getFoerdersatz();

        boolean kikZertifiziert =
                veranstaltung.getVerein() != null
                        && veranstaltung.getVerein()
                        .isKikZertifiziertAm(datum);

        if (kikZertifiziert) {

            KikZuschlag kik =
                    kikZuschlagService.findOptionalGueltigAm(datum);

            if (kik != null
                    && kik.getKikZuschlag() != null) {

                tagessatz =
                        tagessatz.add(kik.getKikZuschlag());
            }
        }

        return tagessatz.min(
                FoerderConfig.FOERDERDECKEL
        );
    }

    public boolean hatGueltigenFoerdersatz(Veranstaltung veranstaltung) {
        if (veranstaltung == null
                || veranstaltung.getTyp() == null
                || veranstaltung.getBeginnDatum() == null
                || !veranstaltung.getTyp().isFoerderfaehig()) {
            return true;
        }

        Foerdersatz foerdersatz =
                foerdersatzService.findOptionalGueltigFuerTypAm(
                        veranstaltung.getTyp(),
                        veranstaltung.getBeginnDatum()
                );

        return foerdersatz != null
                && foerdersatz.getFoerdersatz() != null;
    }


    public BigDecimal berechneAngewandtenFoerdersatz(
            Veranstaltung veranstaltung,
            boolean fallbackAufPlanungssatz
    ) {
        if (veranstaltung == null) {
            return BigDecimal.ZERO;
        }

        VeranstaltungTyp typ = veranstaltung.getTyp();
        LocalDate datum = veranstaltung.getBeginnDatum();

        if (typ == null || datum == null || !typ.isFoerderfaehig()) {
            return BigDecimal.ZERO;
        }

        Foerdersatz foerdersatz =
                foerdersatzService.findOptionalGueltigFuerTypAm(
                        typ,
                        datum
                );

        if (foerdersatz == null
                || foerdersatz.getFoerdersatz() == null) {

            if (fallbackAufPlanungssatz) {

                boolean kikZertifiziert =
                        veranstaltung.getVerein() != null
                                && veranstaltung.getVerein()
                                .isKikZertifiziertAm(datum);

                log.warn(
                        "Kein gültiger Fördersatz für {} am {}. "
                                + "Verwende Planungssatz für die Abrechnung.",
                        typ,
                        datum
                );

                return berechnePlanungsFoerdersatz(
                        datum,
                        kikZertifiziert
                );
            }

            throw new IllegalStateException(
                    ErrorMessages.NO_VALID_FOERDERSATZ
            );
        }

        BigDecimal tagessatz =
                foerdersatz.getFoerdersatz();

        boolean kikZertifiziert =
                veranstaltung.getVerein() != null
                        && veranstaltung.getVerein()
                        .isKikZertifiziertAm(datum);

        if (kikZertifiziert) {

            KikZuschlag kik =
                    kikZuschlagService.findOptionalGueltigAm(datum);

            if (kik != null
                    && kik.getKikZuschlag() != null) {

                tagessatz =
                        tagessatz.add(kik.getKikZuschlag());
            }
        }

        return tagessatz.min(
                FoerderConfig.FOERDERDECKEL
        );
    }

    /**
     * Fördersatz für die Planung.
     *
     * Planung arbeitet immer mit 10 € Basis
     * und berücksichtigt bei aktiviertem KiK den aktuell
     * gültigen bzw. letzten verfügbaren KiK-Zuschlag.
     */
    public BigDecimal berechneAngewandtenFoerdersatz(
            Planung planung
    ) {
        if (planung == null
                || planung.getVeranstaltung() == null) {
            return BigDecimal.ZERO;
        }

        return berechnePlanungsFoerdersatz(
                planung.getVeranstaltung().getBeginnDatum(),
                planung.isKikZertifiziert()
        );
    }


    /**
     * Fördersatz für die Simulation.
     *
     * Basis immer 10 €.
     *
     * KiK wird berücksichtigt, wenn:
     * - der Simulationsschalter aktiviert ist
     *   ODER
     * - bereits ein gültiges KiK-Zertifikat für den Veranstaltungstag besteht.
     */
    public BigDecimal berechneAngewandtenFoerdersatz(
            PlanungsSimulation simulation
    ) {
        if (simulation == null
                || simulation.getVeranstaltung() == null) {
            return BigDecimal.ZERO;
        }

        // WICHTIG:
        // simulation.getVeranstaltung() ist VeranstaltungsInfo,
        // nicht die Entity Veranstaltung.
        boolean kikAktiv =
                simulation.isKikZertifiziert()
                        || simulation.getVeranstaltung()
                        .isVereinKikZertifiziert();

        return berechnePlanungsFoerdersatz(
                simulation.getVeranstaltung().getBeginnDatum(),
                kikAktiv
        );
    }


    /**
     * Planung / Simulation:
     * Basis 10 € plus ggf. KiK.
     */
    private BigDecimal berechnePlanungsFoerdersatz(
            LocalDate datum,
            boolean kikZertifiziert
    ) {
        BigDecimal tagessatz =
                PLANUNG_FOERDERSATZ;

        if (kikZertifiziert) {

            KikZuschlag kik =
                    kikZuschlagService
                            .findOptionalOderLetztenGueltigen(datum);

            if (kik != null
                    && kik.getKikZuschlag() != null) {

                tagessatz =
                        tagessatz.add(
                                kik.getKikZuschlag()
                        );
            }
        }

        return tagessatz.min(
                FoerderConfig.FOERDERDECKEL
        );
    }


    /**
     * Geplante Förderung der Simulation.
     */
    public BigDecimal berechneGeplanteFoerderung(
            PlanungsSimulation simulation
    ) {
        if (simulation == null) {
            return BigDecimal.ZERO;
        }

        return berechneAngewandtenFoerdersatz(simulation)
                .multiply(
                        BigDecimal.valueOf(
                                simulation.getTeilnehmer()
                        )
                )
                .multiply(
                        BigDecimal.valueOf(
                                berechneFoerdertage(simulation)
                        )
                );
    }


    /**
     * Geplante Förderung einer Planung.
     */
    public BigDecimal berechneGeplanteFoerderung(
            Planung planung
    ) {
        if (planung == null) {
            return BigDecimal.ZERO;
        }

        return berechneAngewandtenFoerdersatz(planung)
                .multiply(
                        BigDecimal.valueOf(
                                planung.getTeilnehmer()
                        )
                )
                .multiply(
                        BigDecimal.valueOf(
                                berechneFoerdertage(planung)
                        )
                );
    }
}