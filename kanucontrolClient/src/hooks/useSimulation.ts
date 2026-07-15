import { useCallback, useEffect, useState } from "react";

import {
    getSimulation,
    saveSimulation,
    simulate,
} from "@/api/services/simulationApi";

import { PlanungsSimulation } from "@/api/types/simulation/PlanungsSimulation";
import { SimulationErgebnis } from "@/api/types/simulation/SimulationErgebnis";

export function useSimulation(
    veranstaltungId?: number
) {

    const [simulation, setSimulation] =
        useState<PlanungsSimulation>();

    const [ergebnis, setErgebnis] =
        useState<SimulationErgebnis>();

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState<string>();

    const load = useCallback(async () => {

        if (!veranstaltungId) {
            return;
        }

        try {

            setLoading(true);

            const sim = await getSimulation(veranstaltungId);

            const result = await simulate(sim);

            setSimulation(sim);
            setErgebnis(result);

            setError(undefined);

        } catch (e) {

            console.error(e);

            setError(
                "Simulation konnte nicht geladen werden."
            );

        } finally {

            setLoading(false);
        }

    }, [veranstaltungId]);

    const recalculate = useCallback(async (
        sim: PlanungsSimulation
    ) => {

        try {

            // simulation NICHT überschreiben!

            const result =
                await simulate(sim);

            setErgebnis(result);

            setError(undefined);

        } catch {

            setError(
                "Simulation konnte nicht berechnet werden."
            );
        }

    }, []);

    const save = useCallback(
        async (sim: PlanungsSimulation) => {

            if (!veranstaltungId) {
                return;
            }

            try {

                await saveSimulation(
                    veranstaltungId,
                    sim
                );

                setSimulation(sim);

                setError(undefined);

            } catch {

                setError(
                    "Simulation konnte nicht gespeichert werden."
                );
            }

        },
        [veranstaltungId]
    );

    useEffect(() => {

        load();

    }, [load]);

    return {

        simulation,
        ergebnis,

        loading,
        error,

        recalculate,
        saveSimulation: save,
        reload: load,
    };
}