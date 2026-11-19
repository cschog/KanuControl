import type { Verbrauchsart, Verbrauchsabrechnungsart, Verbrauchspreis } from "@/vermietung/types/Verbrauchskosten";

export interface VerbrauchskostenSave {
  bezeichnung: string;
  verbrauchsart: Verbrauchsart;
  abrechnungsart: Verbrauchsabrechnungsart;
  mengeneinheit: string | null;
  preis: number;
  preise?: Verbrauchspreis[];
}
