package com.kcserver.mapper;

import com.kcserver.dto.simulation.PlanungsSimulation;
import com.kcserver.entity.Planung;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanungSimulationMapper {

    private final VeranstaltungsInfoMapper veranstaltungsInfoMapper;

    public void updatePlanung(
            Planung planung,
            PlanungsSimulation simulation
    ) {

        if (planung == null || simulation == null) {
            return;
        }

        // Simulationsparameter

        planung.setTeilnehmer(
                simulation.getTeilnehmer()
        );

        planung.setMitarbeiter(
                simulation.getMitarbeiter()
        );

        planung.setKikZertifiziert(
                simulation.isKikZertifiziert()
        );

        planung.setTeilnehmerBeitragUnter21Jahre(
                simulation.getTeilnehmerBeitragUnter21Jahre()
        );

        planung.setMitarbeiterBeitrag(
                simulation.getMitarbeiterBeitrag()
        );

        planung.setUnterkunftPreisProPersonUndNacht(
                simulation.getUnterkunftPreisProPersonUndNacht()
        );

        planung.setVerpflegungPreisProPersonUndTag(
                simulation.getVerpflegungPreisProPersonUndTag()
        );

        planung.setHonorare(
                simulation.getHonorare()
        );

        planung.setFahrtkosten(
                simulation.getFahrtkosten()
        );

        planung.setVerbrauchsmaterialProTag(
                simulation.getVerbrauchsmaterialProTag()
        );

        planung.setKultur(
                simulation.getKultur()
        );

        planung.setMiete(
                simulation.getMiete()
        );

        planung.setSonstigeKostenProTag(
                simulation.getSonstigeKostenProTag()
        );

        berechneAntragsdaten(planung);
    }

    public PlanungsSimulation toSimulation(
            Planung planung
    ) {

        if (planung == null) {
            return null;
        }

        return PlanungsSimulation.builder()

                .veranstaltung(

                        veranstaltungsInfoMapper.toDTO(

                                planung.getVeranstaltung()

                        )

                )

                .kikZertifiziert(planung.isKikZertifiziert())

                .teilnehmer(planung.getTeilnehmer())

                .mitarbeiter(planung.getMitarbeiter())

                .teilnehmerBeitragUnter21Jahre(planung.getTeilnehmerBeitragUnter21Jahre())

                .mitarbeiterBeitrag(planung.getMitarbeiterBeitrag())

                .unterkunftPreisProPersonUndNacht(planung.getUnterkunftPreisProPersonUndNacht())

                .verpflegungPreisProPersonUndTag(planung.getVerpflegungPreisProPersonUndTag())

                .honorare(planung.getHonorare())

                .fahrtkosten(planung.getFahrtkosten())

                .verbrauchsmaterialProTag(planung.getVerbrauchsmaterialProTag())

                .kultur(planung.getKultur())

                .miete(planung.getMiete())

                .sonstigeKostenProTag(planung.getSonstigeKostenProTag())

                .build();
    }

    public void berechneAntragsdaten(Planung planung) {

        int tn = planung.getTeilnehmer() != null
                ? planung.getTeilnehmer()
                : 0;

        int ma = planung.getMitarbeiter() != null
                ? planung.getMitarbeiter()
                : 0;

        // Teilnehmer
        planung.setGeplanteTeilnehmerMaennlich((tn + 1) / 2);
        planung.setGeplanteTeilnehmerWeiblich(tn / 2);
        planung.setGeplanteTeilnehmerDivers(0);

        // Mitarbeiter
        planung.setGeplanteMitarbeiterMaennlich((ma + 1) / 2);
        planung.setGeplanteMitarbeiterWeiblich(ma / 2);
        planung.setGeplanteMitarbeiterDivers(0);
    }
}