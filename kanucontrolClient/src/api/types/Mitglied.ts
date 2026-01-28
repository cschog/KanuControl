// api/types/Mitglied.ts
import { MitgliedFunktion } from "@/api/enums/MitgliedFunktion";
import { VereinRef } from "./VereinRef";

/** ============================
 *  LIST / TABLE
 *  ============================ */
export interface MitgliedList {
  id: number;
  vereinId: number;
  vereinName: string;
  vereinAbk: string;
  hauptVerein: boolean;
  funktion?: MitgliedFunktion;
}

/** ============================
 *  DETAIL / EDIT
 *  ============================ */
export interface MitgliedDetail {
  id: number;
  personId: number;
  hauptVerein: boolean;
  funktion?: MitgliedFunktion;
  verein: VereinRef;
}

/** ============================
 *  SAVE / UPDATE
 *  ============================ */
export interface MitgliedSave {
  personId: number;
  vereinId: number;
  hauptVerein: boolean;
  funktion?: MitgliedFunktion | null;
}
// src/api/types/Mitglied.ts
export interface MitgliedSaveInPerson {
  vereinId: number;
  hauptVerein: boolean;
  funktion?: MitgliedFunktion | null;
}
