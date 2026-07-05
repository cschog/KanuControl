package com.kcserver.simulation;

import com.kcserver.entity.Veranstaltung;
import com.kcserver.service.VeranstaltungBerechnungsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;


@Component
@RequiredArgsConstructor
public class PlanungsSimulationFactory {

    private final VeranstaltungBerechnungsService berechnungsService;

    public PlanungsSimulation fromVeranstaltung(
            Veranstaltung v
    ) {

        return PlanungsSimulation.builder()
                .teilnehmer(
                        berechnungsService
                                .ermittleGeplanteTeilnehmer(v)
                )
                .mitarbeiter(
                        berechnungsService
                                .ermittleGeplanteMitarbeiter(v)
                )
                .tage(
                        berechnungsService
                                .ermittleTage(v)
                )
                .naechte(
                        berechnungsService
                                .ermittleNaechte(v)
                )
                .standardGebuehr(
                        v.getStandardGebuehr()
                )
                .beitragsstruktur(
                        v.getBeitragsstruktur()
                )
                .unterkunftPreis(
                        v.getUnterkunftsart() == null
                                ? BigDecimal.ZERO
                                : v.getUnterkunftsart()
                                .getPreisProPersonUndNacht()
                )
                .verpflegungPreis(
                        v.getVerpflegungsmodell() == null
                                ? BigDecimal.ZERO
                                : v.getVerpflegungsmodell()
                                .getPreisProPersonUndTag()
                )
                .typ(v.getTyp())
                .build();
    }
}
