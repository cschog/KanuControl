// src/api/types/veranstaltung/VeranstaltungDetail.ts
import { VeranstaltungTyp } from "@/kjfp/api/enums/VeranstaltungTyp";
import { VereinRef } from "@/core/api/types/verein/VereinRef";
import { PersonRef } from "@/core/api/types/person/PersonRef";
import { CountryCode } from "@/kjfp/api/enums/CountryCode";
import { VerpflegungsmodellRef } from "@/kjfp/types/veranstaltung/VerpflegungsmodellRef";
import { UnterkunftsartRef } from "@/kjfp/types/unterkunft/UnterkunftsartRef";

export interface VeranstaltungDetail {
  id: number;

  name: string;
  typ: VeranstaltungTyp;

  unterkunftsart?: UnterkunftsartRef;
  verpflegungsmodell?: VerpflegungsmodellRef;

  plz?: string;
  ort?: string;
  countryCode?: CountryCode;

  beginnDatum: string;
  beginnZeit: string;
  endeDatum: string;
  endeZeit: string;

  verein: VereinRef;
  leiter: PersonRef;

  beitragsstrukturId?: number;
  beitragsstrukturName?: string;

  aktiv: boolean;
}
