import { FinanzKategorie } from "@/kjfp/types/finanz";

export interface PlanungspositionDTO {
  kategorie: FinanzKategorie;

  betrag: number;

  automatisch: boolean;
}
