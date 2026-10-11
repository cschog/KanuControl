export type Verbrauchsart = "STROM" | "GAS" | "WASSER";

export type Verbrauchsabrechnungsart = "PAUSCHALE" | "TAGESPREIS" | "ZAEHLERSTAND";

export interface Verbrauchskosten {
  id: number;
  mietobjektId: number;
  bezeichnung: string;
  verbrauchsart: Verbrauchsart;
  abrechnungsart: Verbrauchsabrechnungsart;
  mengeneinheit: string | null;
  preis: number;
  aktiv: boolean;
}
