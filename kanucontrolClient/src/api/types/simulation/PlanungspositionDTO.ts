import { FinanzKategorie } from "@/api/types/finanz";

export interface PlanungspositionDTO {

    kategorie: FinanzKategorie;

    betrag: number;

    automatisch: boolean;
}