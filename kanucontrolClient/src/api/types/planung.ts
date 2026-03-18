export type FinanzTyp = "KOSTEN" | "EINNAHME";

export type FinanzKategorie =
  | "UNTERKUNFT"
  | "VERPFLEGUNG"
  | "HONORARE"
  | "FAHRTKOSTEN"
  | "VERBRAUCHSMATERIAL"
  | "KULTUR"
  | "MIETE"
  | "SONSTIGE_KOSTEN"
  | "TEILNEHMERBEITRAG"
  | "PFAND"
  | "KJFP_ZUSCHUSS"
  | "SONSTIGE_EINNAHMEN";

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
