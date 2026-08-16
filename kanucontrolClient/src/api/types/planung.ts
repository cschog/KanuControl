import { FinanzKategorie } from "@/api/types/finanz";

export type PlanungsStatus = "IN_BEARBEITUNG" | "EINGEREICHT";

export interface PlanungPosition {
  id: number;
  kategorie: FinanzKategorie;
  betrag: number;
  kommentar?: string;
  automatischBerechnet: boolean;
  editierbar: boolean;
  menge?: number;
  einzelpreis?: number;
  einheit?: string;
}

export interface PlanungDetail {
  id: number;
  eingereicht: boolean;
  status: PlanungsStatus;
  positionen: PlanungPosition[];
}

export interface PlanungPositionCreate {
  kategorie: FinanzKategorie;
  betrag: number;
  kommentar?: string;
}
