export interface Verbrauchspreis { gueltigVon: string; gueltigBis: string | null; preis: number; }

export type Verbrauchsart = "STROM" | "GAS" | "WASSER";

export type Verbrauchsabrechnungsart = "PAUSCHALE" | "TAGESPREIS" | "ZAEHLERSTAND";

export interface Verbrauchskosten {
  id: number;
  mietobjektId: number;
  bezeichnung: string;
  verbrauchsart: Verbrauchsart;
  abrechnungsart: Verbrauchsabrechnungsart;
  mengeneinheit: string | null;
  preis: number | null;
  preise?: Verbrauchspreis[];
  aktiv: boolean;
}
