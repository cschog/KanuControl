package com.kcserver.service;

import com.kcserver.dto.beitrag.BeitragsVorschlag;
import com.kcserver.entity.Planung;
import com.kcserver.service.veranstaltung.VeranstaltungBerechnungsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.kcserver.dto.simulation.PlanungsSimulation;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PlanungBerechnungService {

    private final FoerderService foerderService;
    private final VeranstaltungBerechnungsService veranstaltungBerechnungsService;
    private static final BigDecimal EMPFOHLENER_BEITRAG_RUNDUNG =
            BigDecimal.valueOf(5);

    private static final int MIN_BEITRAG = 0;
    private static final int MAX_BEITRAG = 1000;
    private static final int BEITRAGSSCHRITT = 5;

    public BigDecimal berechneTeilnehmerbeitraege(
            Planung planung
    ) {

        if (planung == null) {
            return BigDecimal.ZERO;
        }

        return nvl(planung.getTeilnehmerBeitragUnter21Jahre())
                .multiply(BigDecimal.valueOf(planung.getTeilnehmer()))
                .add(
                        nvl(planung.getMitarbeiterBeitrag())
                                .multiply(BigDecimal.valueOf(planung.getMitarbeiter()))
                );
    }

    public BigDecimal berechneTeilnehmerbeitraege(
            PlanungsSimulation simulation
    ) {

        if (simulation == null) {
            return BigDecimal.ZERO;
        }

        return nvl(simulation.getTeilnehmerBeitragUnter21Jahre())
                .multiply(BigDecimal.valueOf(simulation.getTeilnehmer()))
                .add(
                        nvl(simulation.getMitarbeiterBeitrag())
                                .multiply(BigDecimal.valueOf(simulation.getMitarbeiter()))
                );
    }


    public BigDecimal berechneDurchschnittlichenPersonenbeitrag(
            PlanungsSimulation simulation
    ) {

        return durchschnitt(
                berechneTeilnehmerbeitraege(simulation),
                personen(
                        simulation.getTeilnehmer(),
                        simulation.getMitarbeiter()
                )
        );
    }

    public BigDecimal berechneEmpfohlenenPersonenbeitrag(
            PlanungsSimulation simulation
    ){
        return rundeEmpfohlenenBeitrag(
                berechneDurchschnittlichenPersonenbeitrag(simulation)
        );
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static BigDecimal rundeEmpfohlenenBeitrag(BigDecimal wert) {

        if (wert == null || BigDecimal.ZERO.compareTo(wert) == 0) {
            return BigDecimal.ZERO;
        }

        return wert
                .divide(EMPFOHLENER_BEITRAG_RUNDUNG, 0, RoundingMode.HALF_UP)
                .multiply(EMPFOHLENER_BEITRAG_RUNDUNG);
    }


    public BigDecimal berechneUnterkunft(
            Planung planung
    ) {

        if (planung == null
                || planung.getUnterkunftPreisProPersonUndNacht() == null) {
            return BigDecimal.ZERO;
        }

        return planung.getUnterkunftPreisProPersonUndNacht()
                .multiply(
                        BigDecimal.valueOf(
                                planung.getTeilnehmer()
                                        + planung.getMitarbeiter()
                        )
                )
                .multiply(
                        BigDecimal.valueOf(
                                veranstaltungBerechnungsService.ermittleNaechte(planung.getVeranstaltung())
                        )
                );
    }

    public BigDecimal berechneUnterkunft(
            PlanungsSimulation simulation
    ) {

        if (simulation.getUnterkunftPreisProPersonUndNacht() == null) {
            return BigDecimal.ZERO;
        }

        return simulation.getUnterkunftPreisProPersonUndNacht()
                .multiply(
                        BigDecimal.valueOf(
                                simulation.getTeilnehmer()
                                        + simulation.getMitarbeiter()
                        )
                )

                .multiply(
                        BigDecimal.valueOf(
                                simulation.getVeranstaltung().getNaechte()
                        )
                );

    }

    public BigDecimal berechneVerpflegung(
            Planung planung
    ) {

        if (planung == null
                || planung.getVerpflegungPreisProPersonUndTag() == null) {
            return BigDecimal.ZERO;
        }

        long tage = getBerechnungstage(planung);

        return planung.getVerpflegungPreisProPersonUndTag()
                .multiply(
                        BigDecimal.valueOf(
                                planung.getTeilnehmer()
                                        + planung.getMitarbeiter()
                        )
                )
                .multiply(BigDecimal.valueOf(tage));
    }

    public BigDecimal berechneVerpflegung(
            PlanungsSimulation simulation
    ) {
        if (simulation.getVerpflegungPreisProPersonUndTag() == null) {
            return BigDecimal.ZERO;
        }

        return simulation.getVerpflegungPreisProPersonUndTag()
                .multiply(
                        BigDecimal.valueOf(
                                simulation.getTeilnehmer()
                                        + simulation.getMitarbeiter()
                        )
                )
                .multiply(
                        BigDecimal.valueOf(
                                getBerechnungstage(simulation)
                        )
                );
    }

    public BigDecimal berechneKjfpZuschuss(
            Planung planung
    ) {

        if (planung == null) {
            return BigDecimal.ZERO;
        }

        return foerderService.berechneGeplanteFoerderung(planung);
    }

    public BigDecimal berechneKjfpZuschuss(
            PlanungsSimulation simulation
    ) {

        if (simulation == null) {
            return BigDecimal.ZERO;
        }

        return foerderService.berechneGeplanteFoerderung(
                simulation
        );
    }

    public BigDecimal berechneHonorare(
            Planung planung
    ) {
        return planung == null || planung.getHonorare() == null
                ? BigDecimal.ZERO
                : planung.getHonorare();
    }

    public BigDecimal berechneFahrtkosten(
            Planung planung
    ) {
        return planung == null || planung.getFahrtkosten() == null
                ? BigDecimal.ZERO
                : planung.getFahrtkosten();
    }

    public BigDecimal berechneVerbrauchsmaterial(
            Planung planung
    ) {

        if (planung == null
                || planung.getVerbrauchsmaterialProTag() == null) {
            return BigDecimal.ZERO;
        }

        long tage = getBerechnungstage(planung);

        return planung.getVerbrauchsmaterialProTag()
                .multiply(BigDecimal.valueOf(tage));
    }

    public BigDecimal berechneKultur(
            Planung planung
    ) {
        return planung == null || planung.getKultur() == null
                ? BigDecimal.ZERO
                : planung.getKultur();
    }

    public BigDecimal berechneMiete(
            Planung planung
    ) {
        return planung == null || planung.getMiete() == null
                ? BigDecimal.ZERO
                : planung.getMiete();
    }

    public BigDecimal berechneSonstigeKosten(
            Planung planung
    ) {

        if (planung == null
                || planung.getSonstigeKostenProTag() == null) {
            return BigDecimal.ZERO;
        }

        long tage = getBerechnungstage(planung);

        return planung.getSonstigeKostenProTag()
                .multiply(BigDecimal.valueOf(tage));
    }

    public BigDecimal berechneHonorare(
            PlanungsSimulation simulation
    ) {
        return simulation == null || simulation.getHonorare() == null
                ? BigDecimal.ZERO
                : simulation.getHonorare();
    }

    public BigDecimal berechneFahrtkosten(
            PlanungsSimulation simulation
    ) {
        return simulation == null || simulation.getFahrtkosten() == null
                ? BigDecimal.ZERO
                : simulation.getFahrtkosten();
    }


    public BigDecimal berechneVerbrauchsmaterial(
            PlanungsSimulation simulation
    ) {
        if (simulation == null
                || simulation.getVerbrauchsmaterialProTag() == null) {
            return BigDecimal.ZERO;
        }

        return simulation.getVerbrauchsmaterialProTag()
                .multiply(BigDecimal.valueOf(getBerechnungstage(simulation)));
    }

    public BigDecimal berechneKultur(
            PlanungsSimulation simulation
    ) {
        return simulation == null || simulation.getKultur() == null
                ? BigDecimal.ZERO
                : simulation.getKultur();
    }

    public BigDecimal berechneMiete(
            PlanungsSimulation simulation
    ) {
        return simulation == null || simulation.getMiete() == null
                ? BigDecimal.ZERO
                : simulation.getMiete();
    }

    public BigDecimal berechneSonstigeKosten(
            PlanungsSimulation simulation
    ) {
        if (simulation == null
                || simulation.getSonstigeKostenProTag() == null) {
            return BigDecimal.ZERO;
        }

        return simulation.getSonstigeKostenProTag()
                .multiply(BigDecimal.valueOf(getBerechnungstage(simulation)));
    }

    private static int personen(int teilnehmer, int mitarbeiter) {
        return teilnehmer + mitarbeiter;
    }

    private static BigDecimal durchschnitt(BigDecimal summe, int personen) {
        if (personen == 0) {
            return BigDecimal.ZERO;
        }

        return summe.divide(
                BigDecimal.valueOf(personen),
                2,
                RoundingMode.HALF_UP
        );
    }

    public BeitragsVorschlag berechneBeitragsVorschlag(
            PlanungsSimulation simulation
    ) {

        if (simulation == null) {
            return new BeitragsVorschlag(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        int teilnehmer = simulation.getTeilnehmer();
        int mitarbeiter = simulation.getMitarbeiter();

        if (teilnehmer + mitarbeiter == 0) {
            return new BeitragsVorschlag(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        BigDecimal ziel =
                berechneEmpfohlenenPersonenbeitrag(simulation);

        BigDecimal aktuellerTn =
                nvl(simulation.getTeilnehmerBeitragUnter21Jahre());

        BigDecimal aktuellerMa =
                nvl(simulation.getMitarbeiterBeitrag());

        BigDecimal besterTn = aktuellerTn;
        BigDecimal besterMa = aktuellerMa;
        BigDecimal besterDurchschnitt =
                berechneDurchschnittlichenPersonenbeitrag(simulation);

        BigDecimal kleinsteAbweichung =
                besterDurchschnitt.subtract(ziel).abs();

        BigDecimal kleinsteAenderung =
                aktuellerTn.subtract(besterTn).abs()
                        .add(
                                aktuellerMa.subtract(besterMa).abs()
                        );

        for (int tn = MIN_BEITRAG; tn <= MAX_BEITRAG; tn += BEITRAGSSCHRITT) {
            for (int ma = tn; ma <= MAX_BEITRAG; ma += BEITRAGSSCHRITT) {

                BigDecimal summe =
                        BigDecimal.valueOf(tn)
                                .multiply(BigDecimal.valueOf(teilnehmer))
                                .add(
                                        BigDecimal.valueOf(ma)
                                                .multiply(BigDecimal.valueOf(mitarbeiter))
                                );

                BigDecimal durchschnitt =
                        summe.divide(
                                BigDecimal.valueOf(
                                        teilnehmer + mitarbeiter
                                ),
                                2,
                                RoundingMode.HALF_UP
                        );

                BigDecimal abweichung =
                        durchschnitt.subtract(ziel).abs();

                BigDecimal aenderung =
                        aktuellerTn.subtract(BigDecimal.valueOf(tn)).abs()
                                .add(
                                        aktuellerMa.subtract(BigDecimal.valueOf(ma)).abs()
                                );

                boolean besser =
                        abweichung.compareTo(kleinsteAbweichung) < 0
                                || (
                                abweichung.compareTo(kleinsteAbweichung) == 0
                                        && aenderung.compareTo(kleinsteAenderung) < 0
                        );

                if (besser) {

                    kleinsteAbweichung = abweichung;
                    kleinsteAenderung = aenderung;

                    besterTn = BigDecimal.valueOf(tn);
                    besterMa = BigDecimal.valueOf(ma);
                    besterDurchschnitt = durchschnitt;
                }
            }
        }

        return new BeitragsVorschlag(
                besterTn,
                besterMa,
                besterDurchschnitt
        );
    }

    private long getBerechnungstage(Planung planung) {

        if (planung == null || planung.getVeranstaltung() == null) {
            return 0;
        }

        return Math.min(
                veranstaltungBerechnungsService.ermittleTage(planung.getVeranstaltung()),
                21
        );
    }

    private long getBerechnungstage(PlanungsSimulation simulation) {

        if (simulation == null || simulation.getVeranstaltung() == null) {
            return 0;
        }

        return Math.min(
                simulation.getVeranstaltung().getTage(),
                21
        );
    }
}