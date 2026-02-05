// src/api/types/enums/veranstaltungTypLabels.ts
import { VeranstaltungTyp } from "./VeranstaltungTyp";

export const VERANSTALTUNG_TYP_LABELS: Record<VeranstaltungTyp, string> = {
  [VeranstaltungTyp.JEM]: "Jugenderholungsmaßnahme",
  [VeranstaltungTyp.FM]: "Freizeitmaßnahme",
  [VeranstaltungTyp.BM]: "Bildungsveranstaltung",
  [VeranstaltungTyp.GV]: "Großveranstaltung",
};
