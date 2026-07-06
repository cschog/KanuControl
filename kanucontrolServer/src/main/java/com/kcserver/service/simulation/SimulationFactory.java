package com.kcserver.service.simulation;

import com.kcserver.dto.simulation.PlanungsSimulation;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.service.beitrag.BeitragsregelService;
import com.kcserver.service.veranstaltung.VeranstaltungBerechnungsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class SimulationFactory {

    private static final int STANDARD_TEILNEHMER = 10;
    private static final int TEILNEHMER_PRO_MITARBEITER = 5;

    private static final BigDecimal STANDARD_HONORARE = BigDecimal.ZERO;
    private static final BigDecimal STANDARD_FAHRTKOSTEN = BigDecimal.ZERO;
    private static final BigDecimal STANDARD_VERBRAUCHSMATERIAL_PRO_TAG = BigDecimal.valueOf(30);
    private static final BigDecimal STANDARD_KULTUR = BigDecimal.ZERO;
    private static final BigDecimal STANDARD_MIETE = BigDecimal.ZERO;
    private static final BigDecimal STANDARD_SONSTIGE_KOSTEN_PRO_TAG = BigDecimal.valueOf(10);

    private static final BigDecimal STANDARD_PFAND = BigDecimal.ZERO;
    private static final BigDecimal STANDARD_SONSTIGE_EINNAHMEN_PRO_TAG = BigDecimal.valueOf(150);

    private final VeranstaltungBerechnungsService berechnungsService;
    private final BeitragsregelService beitragsregelService;

    /**
     * Übernimmt die aktuellen Werte einer Veranstaltung.
     */
    public PlanungsSimulation fromVeranstaltung(Veranstaltung v) {

        return PlanungsSimulation.builder()
                .teilnehmer(
                        berechnungsService.ermittleGeplanteTeilnehmer(v)
                )
                .mitarbeiter(
                        berechnungsService.ermittleGeplanteMitarbeiter(v)
                )
                .beginnDatum(v.getBeginnDatum())
                .kikZertifiziert(
                        v.getVerein() != null
                                && v.getVerein().isKikZertifiziertAm(v.getBeginnDatum())
                )
                .tage(
                        berechnungsService.ermittleTage(v)
                )
                .naechte(
                        berechnungsService.ermittleNaechte(v)
                )
                .teilnehmerBeitragUnter21Jahre(
                        beitragsregelService.ermittlePlanungsTeilnehmerBeitrag(
                                        v.getBeitragsstruktur()
                                ))
                .mitarbeiterBeitrag(
                        beitragsregelService.ermittlePlanungsMitarbeiterBeitrag(
                                        v.getBeitragsstruktur()
                                ))
                .beitragsstrukturId(
                        v.getBeitragsstruktur() == null
                                ? null
                                : v.getBeitragsstruktur().getId()
                )
                .unterkunftPreisProPersonUndNacht(
                        v.getUnterkunftsart() == null
                                ? BigDecimal.ZERO
                                : v.getUnterkunftsart().getPreisProPersonUndNacht()
                )
                .verpflegungPreisProPersonUndTag(
                        v.getVerpflegungsmodell() == null
                                ? BigDecimal.ZERO
                                : v.getVerpflegungsmodell().getPreisProPersonUndTag()
                )
                .honorare(STANDARD_HONORARE)
                .fahrtkosten(STANDARD_FAHRTKOSTEN)
                .verbrauchsmaterialProTag(STANDARD_VERBRAUCHSMATERIAL_PRO_TAG)
                .kultur(STANDARD_KULTUR)
                .miete(STANDARD_MIETE)
                .sonstigeKostenProTag(STANDARD_SONSTIGE_KOSTEN_PRO_TAG)
                .pfand(STANDARD_PFAND)
                .sonstigeEinnahmenProTag(
                        STANDARD_SONSTIGE_EINNAHMEN_PRO_TAG
                )
                .typ(v.getTyp())
                .build();
    }

    /**
     * Erzeugt eine neue Simulation mit Standardwerten.
     */
    public PlanungsSimulation createDefault(Veranstaltung v) {

        int teilnehmer = STANDARD_TEILNEHMER;
        int mitarbeiter = berechneMitarbeiter(teilnehmer);

        return PlanungsSimulation.builder()
                .teilnehmer(teilnehmer)
                .mitarbeiter(mitarbeiter)
                .beginnDatum(v.getBeginnDatum())
                .kikZertifiziert(
                        v.getVerein() != null
                                && v.getVerein().isKikZertifiziertAm(v.getBeginnDatum())
                )
                .tage(
                        berechnungsService.ermittleTage(v)
                )
                .naechte(
                        berechnungsService.ermittleNaechte(v)
                )
                .teilnehmerBeitragUnter21Jahre(
                        beitragsregelService.ermittlePlanungsTeilnehmerBeitrag(
                                v.getBeitragsstruktur()
                        )
                )
                .mitarbeiterBeitrag(
                        beitragsregelService.ermittlePlanungsMitarbeiterBeitrag(
                                v.getBeitragsstruktur()
                        )
                )
                .beitragsstrukturId(
                        v.getBeitragsstruktur() == null
                                ? null
                                : v.getBeitragsstruktur().getId()
                )
                .unterkunftPreisProPersonUndNacht(
                        v.getUnterkunftsart() == null
                                ? BigDecimal.ZERO
                                : v.getUnterkunftsart().getPreisProPersonUndNacht()
                )
                .verpflegungPreisProPersonUndTag(
                        v.getVerpflegungsmodell() == null
                                ? BigDecimal.ZERO
                                : v.getVerpflegungsmodell().getPreisProPersonUndTag()
                )
                .honorare(STANDARD_HONORARE)
                .fahrtkosten(STANDARD_FAHRTKOSTEN)
                .verbrauchsmaterialProTag(STANDARD_VERBRAUCHSMATERIAL_PRO_TAG)
                .kultur(STANDARD_KULTUR)
                .miete(STANDARD_MIETE)
                .sonstigeKostenProTag(STANDARD_SONSTIGE_KOSTEN_PRO_TAG)
                .pfand(STANDARD_PFAND)
                .sonstigeEinnahmenProTag(
                        STANDARD_SONSTIGE_EINNAHMEN_PRO_TAG
                )
                .typ(v.getTyp())
                .build();
    }

    private int berechneMitarbeiter(int teilnehmer) {
        return (int) Math.ceil((double) teilnehmer / TEILNEHMER_PRO_MITARBEITER);
    }
}