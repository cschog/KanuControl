
export interface Mietobjekt {
  id: number;

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
