// src/api/types/veranstaltung/VeranstaltungSave.ts
import { VeranstaltungTyp } from "@/kjfp/api/enums/VeranstaltungTyp";
import { CountryCode } from "@/kjfp/api/enums/CountryCode";

export type VeranstaltungScope = "VERBAND" | "VEREIN";

export interface VeranstaltungSave {
  id?: number;

  name: string;
  typ?: VeranstaltungTyp;
  kjfpAktiv: boolean;
  fahrtenAktiv: boolean;
  beschreibung?: string;

  unterkunftsartId?: number;
  verpflegungsmodellId?: number;

  beitragsstrukturId?: number;

  countryCode?: CountryCode;
  plz?: string;
  ort?: string;

  beginnDatum: string;
  beginnZeit?: string;
  endeDatum: string;
  endeZeit?: string;

  vereinId: number;
  leiterId?: number;
}
