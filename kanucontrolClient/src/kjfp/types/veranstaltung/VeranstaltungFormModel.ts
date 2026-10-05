import { VeranstaltungTyp } from "@/kjfp/api/enums/VeranstaltungTyp";
import { VereinRef } from "@/kjfp/types/verein/VereinRef";
import { PersonRef } from "@/kjfp/types/person/PersonRef";
import { CountryCode } from "@/kjfp/api/enums/CountryCode";

import { VerpflegungsmodellRef } from "@/kjfp/types/veranstaltung/VerpflegungsmodellRef";
import { UnterkunftsartRef } from "@/kjfp/types/unterkunft/UnterkunftsartRef";

export interface VeranstaltungFormModel {
  id?: number;

  /* ================= Stammdaten ================= */

  name: string;
  typ: VeranstaltungTyp;

  beginnDatum: string;
  beginnZeit: string;

  endeDatum: string;
  endeZeit: string;

  verein?: VereinRef;
  leiter?: PersonRef;

  /* ================= Detailfelder ================= */

  countryCode?: CountryCode;
  plz?: string;
  ort?: string;

  unterkunftsart?: UnterkunftsartRef;
  verpflegungsmodell?: VerpflegungsmodellRef;

  beitragsstrukturId?: number;
}
