import { FinanzKategorie } from "@/api/types/finanz";

export interface SimulationPosition {

    kategorie: FinanzKategorie;

    betrag: number;

    automatisch: boolean;
}