import { FinanzKategorie } from "@/api/types/finanz";

export interface PlanungPosition {
  id: number;
  kategorie: FinanzKategorie;
  betrag: number;
  kommentar?: string;
}

export interface PlanungDetail {
  id: number;
  eingereicht: boolean;
  positionen: PlanungPosition[];
}

export interface PlanungPositionCreate {
  kategorie: FinanzKategorie;
  betrag: number;
  kommentar?: string;
}
