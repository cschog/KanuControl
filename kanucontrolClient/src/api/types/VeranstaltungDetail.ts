// src/api/types/veranstaltung/VeranstaltungDetail.ts
import { VeranstaltungTyp } from "@/api/enums/VeranstaltungTyp";
import { VereinRef } from "@/api/types/VereinRef";
import { PersonRef } from "@/api/types/PersonRef";

export interface VeranstaltungDetail {
  id: number;

  name: string;
  typ: VeranstaltungTyp;

  artDerUnterkunft?: string;
  artDerVerpflegung?: string;

  plz?: string;
  ort?: string;

  beginnDatum: string;
  beginnZeit: string;
  endeDatum: string;
  endeZeit: string;

  verein: VereinRef;
  leiter: PersonRef;

  geplanteTeilnehmerMaennlich?: number;
  geplanteTeilnehmerWeiblich?: number;
  geplanteTeilnehmerDivers?: number;

  geplanteMitarbeiterMaennlich?: number;
  geplanteMitarbeiterWeiblich?: number;
  geplanteMitarbeiterDivers?: number;

  aktiv: boolean;
}
