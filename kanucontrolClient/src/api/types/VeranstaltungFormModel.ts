import { VeranstaltungTyp } from "@/api/enums/VeranstaltungTyp";
import { VereinRef } from "@/api/types/VereinRef";
import { PersonRef } from "@/api/types/PersonRef";

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

  plz?: string;
  ort?: string;

  artDerUnterkunft?: string;
  artDerVerpflegung?: string;

  individuelleGebuehren?: boolean;
  standardGebuehr?: number;

  geplanteTeilnehmerMaennlich?: number;
  geplanteTeilnehmerWeiblich?: number;
  geplanteTeilnehmerDivers?: number;

  geplanteMitarbeiterMaennlich?: number;
  geplanteMitarbeiterWeiblich?: number;
  geplanteMitarbeiterDivers?: number;
}
