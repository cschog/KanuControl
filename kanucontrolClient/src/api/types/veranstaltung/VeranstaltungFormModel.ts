import { VeranstaltungTyp } from "@/api/enums/VeranstaltungTyp";
import { VereinRef } from "@/api/types/verein/VereinRef";
import { PersonRef } from "@/api/types/person/PersonRef";
import { CountryCode } from "@/api/enums/CountryCode";

import { VeranstaltungScope } from "@/api/enums/VeranstaltungScope";

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

  artDerUnterkunft?: string;
  artDerVerpflegung?: string;

  scope: VeranstaltungScope;

  individuelleGebuehren?: boolean;
  standardGebuehr?: number;

  beitragsstrukturId?: number;

  geplanteTeilnehmerMaennlich?: number;
  geplanteTeilnehmerWeiblich?: number;
  geplanteTeilnehmerDivers?: number;

  geplanteMitarbeiterMaennlich?: number;
  geplanteMitarbeiterWeiblich?: number;
  geplanteMitarbeiterDivers?: number;
}
