import { PlanungspositionDTO } from "./PlanungspositionDTO";

export interface SimulationErgebnis {

    positionen: PlanungspositionDTO[];

    kosten: number;

    einnahmen: number;

    saldo: number;
}