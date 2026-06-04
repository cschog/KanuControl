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
  reihenfolge: number;
  beschreibung: string;
  vonPlz: string;
  vonOrt: string;
  vonCountryCode: string | null;
  nachPlz: string;
  nachOrt: string;
  nachCountryCode: string | null;
  kilometer: number;
  anhaenger: boolean;
  mitfahrer: PersonRef[];
}

export interface FahrtabschnittRequest {
  id: number;
  reihenfolge: number;
  beschreibung: string;
  vonPlz: string;
  vonOrt: string;
  vonCountryCode: string | null;
  nachPlz: string;
  nachOrt: string;
  nachCountryCode: string | null;
  kilometer: number;
  anhaenger: boolean;
  mitfahrerIds: number[];
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
  mitfahrer: PersonRef[];
  fahrtabschnitte: FahrtabschnittResponse[];
}

export interface ReisekostenabrechnungUpdateRequest {
  abrechnungsdatum: string;
  bemerkung: string;
  mitfahrerIds: number[];
  fahrtabschnitte: FahrtabschnittRequest[];
}
