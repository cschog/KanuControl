import { useCallback, useEffect, useState } from "react";

import { getSimulation, saveSimulation, simulate } from "@/api/services/simulationApi";

import { PlanungsSimulation } from "@/api/types/simulation/PlanungsSimulation";
import { SimulationErgebnis } from "@/api/types/simulation/SimulationErgebnis";
import { getApiErrorMessage } from "../api/utils/apiError";

interface SimulationNotReady {
  missing: string[];
}

function isSimulationNotReadyError(error: unknown): SimulationNotReady | undefined {
  if (typeof error !== "object" || error === null || !("response" in error)) {
    return undefined;
  }

  const response = (
    error as {
      response?: {
        data?: {
          error?: string;
          missing?: string[];
        };
      };
    }
  ).response;

  const data = response?.data;

  if (data?.error !== "SIMULATION_NOT_READY" || !Array.isArray(data.missing)) {
    return undefined;
  }

  return {
    missing: data.missing,
  };
}

export function useSimulation(veranstaltungId?: number) {
  const [simulation, setSimulation] = useState<PlanungsSimulation>();
  const [ergebnis, setErgebnis] = useState<SimulationErgebnis>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>();
  const [simulationNotReady, setSimulationNotReady] = useState<SimulationNotReady>();

  /* =========================================================
     ERROR
     ========================================================= */

  const clearError = useCallback(() => {
    setError(undefined);
  }, []);

  /* =========================================================
     LOAD
     ========================================================= */

  const load = useCallback(async () => {
    if (!veranstaltungId) {
      return;
    }

    try {
      setLoading(true);
      setSimulationNotReady(undefined);
      setError(undefined);

      const sim = await getSimulation(veranstaltungId);
      const result = await simulate(sim);

      setSimulation(sim);
      setErgebnis(result);
    } catch (e: unknown) {
      console.error("Fehler beim Laden der Simulation", e);

      const notReady = isSimulationNotReadyError(e);

      if (notReady) {
        setSimulation(undefined);
        setErgebnis(undefined);
        setSimulationNotReady(notReady);
        setError(undefined);
      } else {
        setError(getApiErrorMessage(e));
      }
    } finally {
      setLoading(false);
    }
  }, [veranstaltungId]);

  /* =========================================================
     RECALCULATE
     ========================================================= */

  const recalculate = useCallback(async (sim: PlanungsSimulation) => {
    try {
      const result = await simulate(sim);

      setErgebnis(result);
      setError(undefined);
    } catch (e: unknown) {
      console.error("Fehler bei der Simulationsberechnung", e);
      setError(getApiErrorMessage(e));
    }
  }, []);

  /* =========================================================
     SAVE
     ========================================================= */

  const save = useCallback(
    async (sim: PlanungsSimulation) => {
      if (!veranstaltungId) {
        return false;
      }

      try {
        await saveSimulation(veranstaltungId, sim);

        setSimulation(sim);
        setError(undefined);

        return true;
      } catch (e: unknown) {
        console.error("Fehler beim Speichern der Simulation", e);
        setError(getApiErrorMessage(e));

        return false;
      }
    },
    [veranstaltungId],
  );

  useEffect(() => {
    load();
  }, [load]);

  return {
    simulation,
    ergebnis,
    loading,
    error,
    simulationNotReady,

    recalculate,
    saveSimulation: save,
    reload: load,
    clearError,
  };
}
