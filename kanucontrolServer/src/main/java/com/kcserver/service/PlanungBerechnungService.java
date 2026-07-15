package com.kcserver.service;

import com.kcserver.entity.Planung;
import com.kcserver.service.veranstaltung.VeranstaltungBerechnungsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.kcserver.dto.simulation.PlanungsSimulation;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PlanungBerechnungService {

    private final FoerderService foerderService;
    private final VeranstaltungBerechnungsService veranstaltungBerechnungsService;

    public BigDecimal berechneTeilnehmerbeitraege(
            Planung planung
    ) {

        if (planung == null) {
            return BigDecimal.ZERO;
        }

        return planung.getTeilnehmerBeitragUnter21Jahre()
                .multiply(BigDecimal.valueOf(planung.getTeilnehmer()))
                .add(
                        planung.getMitarbeiterBeitrag()
                                .multiply(BigDecimal.valueOf(planung.getMitarbeiter()))
                );
    }

    public BigDecimal berechneTeilnehmerbeitraege(
            PlanungsSimulation simulation
    ) {

        if (simulation == null) {
            return BigDecimal.ZERO;
        }

        return simulation.getTeilnehmerBeitragUnter21Jahre()
                .multiply(BigDecimal.valueOf(simulation.getTeilnehmer()))
                .add(
                        simulation.getMitarbeiterBeitrag()
                                .multiply(BigDecimal.valueOf(simulation.getMitarbeiter()))
                );
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

        long tage = veranstaltungBerechnungsService
                .ermittleTage(planung.getVeranstaltung());

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
                                simulation.getVeranstaltung().getTage()
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

        long tage = veranstaltungBerechnungsService
                .ermittleTage(planung.getVeranstaltung());

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

        long tage = veranstaltungBerechnungsService
                .ermittleTage(planung.getVeranstaltung());

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
                .multiply(BigDecimal.valueOf(simulation.getVeranstaltung().getTage()));
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
                .multiply(BigDecimal.valueOf(simulation.getVeranstaltung().getTage()));
    }
}