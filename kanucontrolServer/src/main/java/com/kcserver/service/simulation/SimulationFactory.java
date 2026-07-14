package com.kcserver.service.simulation;

import com.kcserver.dto.simulation.PlanungsSimulation;
import com.kcserver.dto.simulation.VeranstaltungsInfo;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.service.beitrag.BeitragsregelService;
import com.kcserver.service.veranstaltung.VeranstaltungBerechnungsService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
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

    private final VeranstaltungBerechnungsService berechnungsService;
    private final BeitragsregelService beitragsregelService;


    /**
     * Übernimmt die aktuellen Werte einer Veranstaltung.
     */
    public PlanungsSimulation fromVeranstaltung(Veranstaltung v) {

        VeranstaltungsInfo info = createVeranstaltungsInfo(v);

        return PlanungsSimulation.builder()
                .veranstaltung(info)
                .kikZertifiziert(info.isVereinKikZertifiziert())
                .teilnehmer(
                        berechnungsService.ermittleGeplanteTeilnehmer(v)
                )
                .mitarbeiter(
                        berechnungsService.ermittleGeplanteMitarbeiter(v)
                )

                .veranstaltung(
                        createVeranstaltungsInfo(
                                v
                        )
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


                .build();
    }

    /**
     * Erzeugt eine neue Simulation mit Standardwerten.
     */
    public PlanungsSimulation createDefault(Veranstaltung v) {

        int teilnehmer = STANDARD_TEILNEHMER;
        int mitarbeiter = berechneMitarbeiter(teilnehmer);

        VeranstaltungsInfo info = createVeranstaltungsInfo(v);

        return PlanungsSimulation.builder()
                .veranstaltung(info)
                .kikZertifiziert(info.isVereinKikZertifiziert())
                .teilnehmer(teilnehmer)
                .mitarbeiter(mitarbeiter)

                .veranstaltung(
                        createVeranstaltungsInfo(
                                v
                        )
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


                .build();
    }

    private int berechneMitarbeiter(int teilnehmer) {
        return (int) Math.ceil((double) teilnehmer / TEILNEHMER_PRO_MITARBEITER);
    }

    private VeranstaltungsInfo createVeranstaltungsInfo(
            @NonNull Veranstaltung v
    ) {


        boolean kik = v.getVerein() != null
                && v.getVerein().isKikZertifiziertAm(v.getBeginnDatum());

        return VeranstaltungsInfo.builder()
                .id(v.getId())
                .name(v.getName())
                .beginnDatum(v.getBeginnDatum())
                .endeDatum(v.getEndeDatum())
                .typ(v.getTyp())
                .tage(berechnungsService.ermittleTage(v))
                .naechte(berechnungsService.ermittleNaechte(v))
                .vereinKikZertifiziert(kik)

                .beitragsstrukturId(
                        v.getBeitragsstruktur() == null
                                ? null
                                : v.getBeitragsstruktur().getId()
                )
                .beitragsstrukturName(
                        v.getBeitragsstruktur() == null
                                ? null
                                : v.getBeitragsstruktur().getName()
                )

                .unterkunftsartId(
                        v.getUnterkunftsart() == null
                                ? null
                                : v.getUnterkunftsart().getId()
                )
                .unterkunftsartName(
                        v.getUnterkunftsart() == null
                                ? null
                                : v.getUnterkunftsart().getBezeichnung()
                )

                .verpflegungsmodellId(
                        v.getVerpflegungsmodell() == null
                                ? null
                                : v.getVerpflegungsmodell().getId()
                )
                .verpflegungsmodellName(
                        v.getVerpflegungsmodell() == null
                                ? null
                                : v.getVerpflegungsmodell().getBezeichnung()
                )

                .build();
    }

}