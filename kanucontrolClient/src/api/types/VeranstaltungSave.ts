// src/api/types/veranstaltung/VeranstaltungSave.ts
import { VeranstaltungTyp } from "@/api/enums/VeranstaltungTyp";

export interface VeranstaltungSave {
  id?: number;

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

  vereinId: number;
  leiterId: number;

  geplanteTeilnehmerMaennlich?: number;
  geplanteTeilnehmerWeiblich?: number;
  geplanteTeilnehmerDivers?: number;

  geplanteMitarbeiterMaennlich?: number;
  geplanteMitarbeiterWeiblich?: number;
  geplanteMitarbeiterDivers?: number;
}
