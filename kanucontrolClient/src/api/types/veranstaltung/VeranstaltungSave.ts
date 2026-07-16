// src/api/types/veranstaltung/VeranstaltungSave.ts
import { VeranstaltungTyp } from "@/api/enums/VeranstaltungTyp";
import { CountryCode } from "@/api/enums/CountryCode";
import { UnterkunftsartRef } from "@/api/types/unterkunft/UnterkunftsartRef";
import { VerpflegungsmodellRef } from "@/api/types/veranstaltung/VerpflegungsmodellRef";

export type VeranstaltungScope = "VERBAND" | "VEREIN";

export interface VeranstaltungSave {
  id?: number;

  name: string;
  typ: VeranstaltungTyp;

  unterkunftsartId?: number;
  verpflegungsmodellId?: number;

  beitragsstrukturId?: number;

  countryCode?: CountryCode;
  plz?: string;
  ort?: string;

  beginnDatum: string;
  beginnZeit: string;
  endeDatum: string;
  endeZeit: string;

  vereinId: number;
  leiterId: number;

}
