// src/api/types/PersonList.ts

export interface PersonList {
  id: number;
  vorname: string;
  name: string;

  /** berechnet im Backend */
  alter?: number;

  /** Wohnort */
  ort?: string;

  /** Abkürzung des Hauptvereins (z. B. "EKC") */
  hauptvereinAbk?: string;

  /** Anzahl der Vereinsmitgliedschaften */
  mitgliedschaftenCount: number;
}
