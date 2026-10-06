export interface Mietobjekt {
  id: number;

  bezeichnung: string;
  beschreibung?: string;

  strasse?: string;
  plz?: string;
  ort?: string;
  countryCode?: string;

  aktiv: boolean;
  mietbar: boolean;
}
