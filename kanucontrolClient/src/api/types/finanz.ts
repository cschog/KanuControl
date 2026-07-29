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

/* =========================================================
   DOMAIN MAPPING
   ========================================================= */

export const kategorieZuTyp: Record<FinanzKategorie, FinanzTyp> = {
  UNTERKUNFT: "KOSTEN",
  VERPFLEGUNG: "KOSTEN",
  HONORARE: "KOSTEN",
  FAHRTKOSTEN: "KOSTEN",
  VERBRAUCHSMATERIAL: "KOSTEN",
  KULTUR: "KOSTEN",
  MIETE: "KOSTEN",
  SONSTIGE_KOSTEN: "KOSTEN",

  TEILNEHMERBEITRAG: "EINNAHME",
  PFAND: "EINNAHME",
  KJFP_ZUSCHUSS: "EINNAHME",
  SONSTIGE_EINNAHMEN: "EINNAHME",
};

export const finanzKategorieLabel: Record<FinanzKategorie, string> = {
  UNTERKUNFT: "Unterkunft",
  VERPFLEGUNG: "Verpflegung",
  HONORARE: "Honorare",
  FAHRTKOSTEN: "Fahrtkosten",
  VERBRAUCHSMATERIAL: "Verbrauchsmaterial",
  KULTUR: "Kultur",
  MIETE: "Miete",
  SONSTIGE_KOSTEN: "Sonstige Kosten",

  TEILNEHMERBEITRAG: "Teilnehmerbeitrag",
  PFAND: "Pfand",
  KJFP_ZUSCHUSS: "KJFP-Zuschuss",
  SONSTIGE_EINNAHMEN: "Sonstige Einnahmen",
};