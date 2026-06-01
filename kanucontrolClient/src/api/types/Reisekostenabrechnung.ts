// api/types/Reisekostenabrechnung.ts

import { PersonRef } from "@/api/types/PersonRef";

export interface ReisekostenabrechnungListResponse {
  id: number;
  fahrerId: number;
  fahrerName: string;
  gesamtKilometer: number;
  gesamtBetrag: number;
}

export interface FahrtabschnittResponse {
  id: number;

  reihenfolge: number | null;

  beschreibung: string | null;

  vonOrt: string;

  nachOrt: string;

  kilometer: number;

  anhaenger: boolean;

  mitfahrer: PersonRef[];
}

export interface ReisekostenabrechnungDetailResponse {
  id: number;

  veranstaltungId: number;

  veranstaltungName: string;

  fahrerId: number;

  fahrerName: string;

  abrechnungsdatum: string;

  gesamtKilometer: number;

  gesamtBetrag: number;

  bemerkung: string | null;

  fahrtabschnitte: FahrtabschnittResponse[];
}

