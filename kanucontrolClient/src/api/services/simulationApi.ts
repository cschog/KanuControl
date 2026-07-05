import apiClient from "@/api/client/apiClient";
import { PlanungsSimulation } from "../types/simulation/PlanungsSimulation";
import { SimulationErgebnis } from "../types/simulation/SimulationErgebnis";

export async function getSimulation(
    veranstaltungId: number
): Promise<PlanungsSimulation> {

    const response =
        await apiClient.get(
            `/simulation/${veranstaltungId}`
        );

    return response.data;
}

export async function simulate(
    simulation: PlanungsSimulation
): Promise<SimulationErgebnis> {

    const response =
        await apiClient.post(
            "/simulation",
            simulation
        );

    return response.data;
}