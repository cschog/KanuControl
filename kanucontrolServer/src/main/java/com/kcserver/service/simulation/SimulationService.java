package com.kcserver.service.simulation;

import com.kcserver.dto.simulation.PlanungsSimulation;
import com.kcserver.entity.Planung;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.PlanungsStatus;
import com.kcserver.exception.ErrorMessages;
import com.kcserver.exception.SimulationVoraussetzungenException;
import com.kcserver.mapper.PlanungSimulationMapper;
import com.kcserver.repository.PlanungRepository;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.service.planung.PlanungAutomatikService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimulationService {

    private final PlanungRepository planungRepository;
    private final VeranstaltungRepository veranstaltungRepository;

    private final SimulationFactory simulationFactory;
    private final PlanungSimulationMapper mapper;

    private final PlanungAutomatikService planungAutomatikService;

    @Transactional
    public PlanungsSimulation getSimulation(
            Long veranstaltungId
    ) {

        Veranstaltung veranstaltung =
                veranstaltungRepository.findById(veranstaltungId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        ErrorMessages.VERANSTALTUNG_NOT_FOUND
                                ));

        pruefeSimulationVoraussetzungen(veranstaltung);

        return planungRepository
                .findByVeranstaltungId(veranstaltungId)
                .map(planung -> {

                    if (!planung.isInitialisiert()
                            && !planung.istEingereicht()) {

                        mapper.updatePlanung(
                                planung,
                                simulationFactory.fromVeranstaltung(
                                        veranstaltung
                                )
                        );

                        planung.setInitialisiert(true);

                        planungRepository.save(planung);
                    }

                    return mapper.toSimulation(planung);
                })
                .orElseGet(() ->
                        simulationFactory.fromVeranstaltung(
                                veranstaltung
                        )
                );
    }

    @Transactional
    public void saveSimulation(
            Long veranstaltungId,
            PlanungsSimulation simulation
    ) {

        Veranstaltung veranstaltung =
                veranstaltungRepository.findById(veranstaltungId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        ErrorMessages.VERANSTALTUNG_NOT_FOUND
                                ));

        pruefeSimulationVoraussetzungen(veranstaltung);

        Planung planung = planungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseGet(() -> createPlanung(veranstaltungId));

        if (planung.istEingereicht()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Die Planung wurde bereits eingereicht und ist gesperrt. "
                            + "Bitte öffnen Sie die Planung zunächst wieder."
            );
        }

        mapper.updatePlanung(
                planung,
                simulation
        );

        planung.setInitialisiert(true);
        mapper.berechneAntragsdaten(planung);

        planungAutomatikService
                .aktualisiereAutomatischePositionen(planung);

        planungRepository.save(planung);
    }

    public void pruefeSimulationVoraussetzungen(
            PlanungsSimulation simulation
    ) {

        if (simulation == null
                || simulation.getVeranstaltung() == null
                || simulation.getVeranstaltung().getId() == null) {

            throw new SimulationVoraussetzungenException(
                    List.of("Veranstaltung")
            );
        }

        Veranstaltung veranstaltung =
                veranstaltungRepository.findById(
                                simulation.getVeranstaltung().getId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        ErrorMessages.VERANSTALTUNG_NOT_FOUND
                                )
                        );

        pruefeSimulationVoraussetzungen(veranstaltung);
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private void pruefeSimulationVoraussetzungen(
            Veranstaltung veranstaltung
    ) {
        List<String> fehlende = new ArrayList<>();

        if (veranstaltung.getBeitragsstruktur() == null) {
            fehlende.add("BEITRAGSSTRUKTUR");
        }

        if (veranstaltung.getVerpflegungsmodell() == null) {
            fehlende.add("VERPFLEGUNGSMODELL");
        }

        if (veranstaltung.getUnterkunftsart() == null) {
            fehlende.add("UNTERKUNFTSART");
        }

        if (!fehlende.isEmpty()) {
            throw new SimulationVoraussetzungenException(fehlende);
        }
    }

    private Planung createPlanung(Long veranstaltungId) {

        Veranstaltung veranstaltung =
                veranstaltungRepository.findById(veranstaltungId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        ErrorMessages.VERANSTALTUNG_NOT_FOUND
                                ));

        pruefeSimulationVoraussetzungen(veranstaltung);

        Planung planung = new Planung();
        planung.setVeranstaltung(veranstaltung);
        planung.setStatus(PlanungsStatus.IN_BEARBEITUNG);

        mapper.updatePlanung(
                planung,
                simulationFactory.fromVeranstaltung(veranstaltung)
        );

        planung.setInitialisiert(true);

        return planung;
    }
}