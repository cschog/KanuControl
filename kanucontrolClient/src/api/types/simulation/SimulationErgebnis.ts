import { SimulationPosition } from "./SimulationPosition";

export interface SimulationErgebnis {

    positionen: SimulationPosition[];

    kosten: number;

    einnahmen: number;

    saldo: number;
}