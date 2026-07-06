import { useCallback, useEffect, useState } from "react";

import {
    getSimulation,
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

            const sim = await getSimulation(
                veranstaltungId
            );

            setSimulation(sim);

            const result =
                await simulate(sim);

            setErgebnis(result);

            setError(undefined);

        } catch {

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

    useEffect(() => {

        load();

    }, [load]);

    return {

        simulation,
        ergebnis,

        loading,
        error,

        recalculate,
        reload: load,
    };
}