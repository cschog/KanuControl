package com.kcserver.kjfp.service.planung;

import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.kjfp.entity.planung.Planung;
import com.kcserver.kjfp.entity.planung.PlanungPosition;
import com.kcserver.kjfp.mapper.PlanungKopieMapper;
import com.kcserver.kjfp.mapper.PlanungSimulationMapper;
import com.kcserver.kjfp.repository.PlanungRepository;
import com.kcserver.kjfp.service.simulation.SimulationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanungKopierService {
    private final PlanungRepository repository;
    private final PlanungKopieMapper mapper;
    private final SimulationFactory simulationFactory;
    private final PlanungSimulationMapper simulationMapper;
    private final PlanungAutomatikService automatik;

    /** Teil derselben Mandantentransaktion wie das Anlegen der Veranstaltung. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void kopieren(Long quelleId, Veranstaltung ziel) {
        Planung kopie = repository.findByVeranstaltungIdWithPositionen(quelleId)
                .map(mapper::kopie)
                .orElseGet(() -> {
                    Planung neu = new Planung();
                    simulationMapper.updatePlanung(neu, simulationFactory.fromVeranstaltung(ziel));
                    neu.setInitialisiert(true);
                    return neu;
                });
        kopie.setVeranstaltung(ziel);
        for (PlanungPosition position : kopie.getPositionen()) position.setPlanung(kopie);

        // Mit den neuen Terminen/Förderbedingungen rechnen. Bestehende fachliche
        // Positionen und ihre Kommentare/Mengen bleiben auch bei älteren Planungen erhalten.
        Planung berechnet = mapper.kopie(kopie);
        berechnet.setVeranstaltung(ziel);
        automatik.aktualisiereAutomatischePositionen(berechnet);
        for (PlanungPosition aktuell : berechnet.getPositionen()) {
            PlanungPosition position = kopie.getPositionen().stream()
                    .filter(p -> p.getKategorie() == aktuell.getKategorie())
                    .findFirst().orElse(null);
            if (position == null) {
                kopie.addPosition(mapper.kopie(aktuell));
            } else if (aktuell.isAutomatischBerechnet()) {
                position.setBetrag(aktuell.getBetrag());
                position.setAutomatischBerechnet(true);
                position.setEditierbar(aktuell.isEditierbar());
            }
        }
        repository.save(kopie);
    }
}
