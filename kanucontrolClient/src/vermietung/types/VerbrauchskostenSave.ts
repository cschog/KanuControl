import type { Verbrauchsart, Verbrauchsabrechnungsart } from "@/vermietung/types/Verbrauchskosten";

export interface VerbrauchskostenSave {
  bezeichnung: string;
  verbrauchsart: Verbrauchsart;
  abrechnungsart: Verbrauchsabrechnungsart;
  mengeneinheit: string | null;
  preis: number;
  aktiv: boolean;
}
