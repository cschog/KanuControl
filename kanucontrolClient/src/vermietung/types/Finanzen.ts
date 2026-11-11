export type Finanzkategorie = "ANZAHLUNG" | "KAUTION" | "MIETE" | "VERBRAUCHSKOSTEN";
export type Finanzbewegungsart = "EINGANG" | "RUECKZAHLUNG" | "VERRECHNUNG";
export interface Finanzbewegung {
  id: number;
  kategorie: Finanzkategorie;
  art: Finanzbewegungsart;
  betrag: number;
  datum: string;
  bemerkung?: string | null;
}
export interface BuchungFinanzen {
 entschaedigungen: import('./EntschaedigungPosition').EntschaedigungPosition[];
 entschaedigungSoll: number;
 nebenkostenSoll: number;
 abrechnungGesamt: number;
  saldo: number;
  stornoEinbehalt?: number;
  buchungId: number;
  buchungsnummer: string;
  mieter: string;
  mietobjekt: string;
  buchungsquelle: "DIREKT" | "AIRBNB";
  buchungsstatus: string;
  anreise: string | null;
  abreise: string | null;
  unbefristet: boolean;
  anzahlungSoll: number;
  kautionSoll: number;
  mietkosten: { bezeichnung: string; anzahl: number; einzelpreis: number; gesamtbetrag: number; abrechnungseinheiten?: number; berechnungsart?: string }[];
  mietrabatt: number;
  mieteSoll: number;
  verbrauchSoll: number;
  verbrauchVorschauVollstaendig: boolean;
  bewegungen: Finanzbewegung[];
}
