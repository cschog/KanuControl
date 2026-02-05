// src/api/types/veranstaltung/VeranstaltungFormModel.ts

import { VeranstaltungTyp } from "@/api/enums/VeranstaltungTyp";
import { VereinRef } from "@/api/types/VereinRef";
import { PersonRef } from "@/api/types/PersonRef";

export interface VeranstaltungFormModel {
  id?: number;

  /* =========================
     Stammdaten
     ========================= */

  name: string;
  typ?: VeranstaltungTyp;

  artDerUnterkunft?: string;
  artDerVerpflegung?: string;

  plz?: string;
  ort?: string;

  beginnDatum?: string; // ISO-Date für DatePicker
  beginnZeit?: string; // HH:mm

  endeDatum?: string;
  endeZeit?: string;

  /* =========================
     Beziehungen (Refs!)
     ========================= */

  verein?: VereinRef;
  leiter?: PersonRef;

  /* =========================
     Plan-Zahlen
     ========================= */

  geplanteTeilnehmerMaennlich?: number;
  geplanteTeilnehmerWeiblich?: number;
  geplanteTeilnehmerDivers?: number;

  geplanteMitarbeiterMaennlich?: number;
  geplanteMitarbeiterWeiblich?: number;
  geplanteMitarbeiterDivers?: number;

  /* =========================
     Status
     ========================= */

  aktiv?: boolean;
}
