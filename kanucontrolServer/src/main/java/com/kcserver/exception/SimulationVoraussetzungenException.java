package com.kcserver.exception;

import java.util.List;

public class SimulationVoraussetzungenException extends RuntimeException {

    private final List<String> fehlendeVoraussetzungen;

    public SimulationVoraussetzungenException(
            List<String> fehlendeVoraussetzungen
    ) {
        super(
                "Die Simulation kann noch nicht gestartet werden. "
                        + "Es fehlen: "
                        + String.join(", ", fehlendeVoraussetzungen)
        );

        this.fehlendeVoraussetzungen = fehlendeVoraussetzungen;
    }

    public List<String> getFehlendeVoraussetzungen() {
        return fehlendeVoraussetzungen;
    }
}