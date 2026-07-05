// src/api/types/veranstaltung/VeranstaltungDetail.ts
import { VeranstaltungTyp } from "@/api/enums/VeranstaltungTyp";
import { VeranstaltungScope } from "@/api/enums/VeranstaltungScope";
import { VereinRef } from "@/api/types/verein/VereinRef";
import { PersonRef } from "@/api/types/person/PersonRef";
import { CountryCode } from "@/api/enums/CountryCode";
import { VerpflegungsmodellRef } from "@/api/types/veranstaltung/VerpflegungsmodellRef";
import { UnterkunftsartRef } from "@/api/types/unterkunft/UnterkunftsartRef";

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

  scope: VeranstaltungScope;

  individuelleGebuehren: boolean;
  standardGebuehr: number;

  beitragsstrukturId?: number;
  beitragsstrukturName?: string;

  geplanteTeilnehmerMaennlich?: number;
  geplanteTeilnehmerWeiblich?: number;
  geplanteTeilnehmerDivers?: number;

  geplanteMitarbeiterMaennlich?: number;
  geplanteMitarbeiterWeiblich?: number;
  geplanteMitarbeiterDivers?: number;

  aktiv: boolean;
}
