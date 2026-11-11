import type { Bankverbindung } from "./Bankverbindung";

export interface Mietobjekt {
  bankverbindungPrimaer?: Bankverbindung | null;
  bankverbindungAlternativ?: Bankverbindung | null;
  id: number;

  kurzfristgrenzeTage?: number;
  bezeichnung: string;
  sortierung: number;
  beschreibung?: string;

  strasse?: string;
  plz?: string;
  ort?: string;
  countryCode?: string;

  anreisezeit: string | null;
  abreisezeit: string | null;

  aktiv: boolean;
  mietbar: boolean;

  direktbuchungAktiv: boolean;
  airbnbAktiv: boolean;
  airbnbInserat: boolean;
}
