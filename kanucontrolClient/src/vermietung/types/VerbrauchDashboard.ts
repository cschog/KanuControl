export interface VerbrauchMonat {
  monat: number; verbrauch: number; mitBuchung: number; ohneBuchung: number;
  belegungsquote: number | null; abgedeckteStunden: number; geschaetzt: boolean;
}
export interface VerbrauchZaehler {
  id: number; bezeichnung: string; verbrauchsart: string; einheit: string; aktiv: boolean; gemeinsam: boolean; letzteAblesung: string | null;
  kennzahlen: { gesamt: number; mitBuchung: number; ohneBuchung: number; pro24hMit: number | null; pro24hOhne: number | null; abgedeckteStunden: number; zeitraumStunden: number; abdeckungProzent: number };
  monate: VerbrauchMonat[];
  intervalle: { von: string; bis: string; ablesungVon: string; ablesungBis: string; verbrauch: number; mitBuchung: number; ohneBuchung: number; belegungsquote: number; geschaetzt: boolean; buchungen: string[] }[];
  buchungen: { id: number; nummer: string | null; status: string; von: string; bis: string }[];
  korrelation: { pearson: number | null; intervalle: number; punkte: { von: string; bis: string; belegungsquote: number; pro24h: number }[] };
  hinweise: string[];
}
export interface VerbrauchDashboard { jahr: number; mietobjektId: number; mietobjekt: string; stand: string; zaehler: VerbrauchZaehler[] }
