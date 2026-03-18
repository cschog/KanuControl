export type BuchungTyp = "EINNAHME" | "AUSGABE";

export type BuchungKategorie =
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

export interface Buchung {
  id: number;
  typ: BuchungTyp;
  kategorie: BuchungKategorie;
  datum: string;
  betrag: number;
  belegNr?: string;
  beschreibung?: string;
}

export interface BuchungCreate {
  typ: BuchungTyp;
  kategorie: BuchungKategorie;
  datum: string;
  betrag: number;
  belegNr?: string;
  beschreibung?: string;
}
