package com.kcserver.service.simulation;

import com.kcserver.dto.simulation.PlanungsSimulation;
import com.kcserver.dto.simulation.VeranstaltungsInfo;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.mapper.VeranstaltungsInfoMapper;
import com.kcserver.service.beitrag.BeitragsregelService;
import com.kcserver.service.veranstaltung.VeranstaltungBerechnungsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class SimulationFactory {

    private final VeranstaltungsInfoMapper veranstaltungsInfoMapper;

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

        VeranstaltungsInfo info =
                veranstaltungsInfoMapper.toDTO(v);

        PlanungsSimulation simulation = PlanungsSimulation.builder()
                .veranstaltung(info)
                .kikZertifiziert(info.isVereinKikZertifiziert())
                .teilnehmer(STANDARD_TEILNEHMER)
                .mitarbeiter(Math.max(1, STANDARD_TEILNEHMER / TEILNEHMER_PRO_MITARBEITER))
                .teilnehmerBeitragUnter21Jahre(
                        beitragsregelService.ermittlePlanungsTeilnehmerBeitrag(
                                v.getBeitragsstruktur()
                        ))
                .mitarbeiterBeitrag(
                        beitragsregelService.ermittlePlanungsMitarbeiterBeitrag(
                                v.getBeitragsstruktur()
                        ))
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

        System.out.println("TN-Beitrag: " + simulation.getTeilnehmerBeitragUnter21Jahre());
        System.out.println("MA-Beitrag: " + simulation.getMitarbeiterBeitrag());
        System.out.println("Unterkunft: " + simulation.getUnterkunftPreisProPersonUndNacht());
        System.out.println("Verpflegung: " + simulation.getVerpflegungPreisProPersonUndTag());

        return simulation;
    }

}