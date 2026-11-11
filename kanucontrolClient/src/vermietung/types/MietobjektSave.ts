import type { Bankverbindung } from "./Bankverbindung";

export interface MietobjektSave {
  bankverbindungPrimaer?: Bankverbindung | null;
  bankverbindungAlternativ?: Bankverbindung | null;
  kurzfristgrenzeTage?: number;
  bezeichnung: string;
  beschreibung: string;
  strasse: string;
  plz: string;
  ort: string;
  countryCode: string;
  anreisezeit: string | null;
  abreisezeit: string | null;
  aktiv: boolean;
  mietbar: boolean;
  direktbuchungAktiv: boolean;
  airbnbAktiv: boolean;
  airbnbInserat: boolean;
}
