import type { Verbrauchsart } from "@/vermietung/enums/Verbrauchsart";
import type { Verbrauchsabrechnungsart } from "@/vermietung/enums/Verbrauchsabrechnungsart";

export interface BuchungVerbrauchskosten {
  id: number;
  verbrauchskostenId: number;
  bezeichnung: string;
  verbrauchsart: Verbrauchsart;
  abrechnungsart: Verbrauchsabrechnungsart;
  mengeneinheit?: string | null;
  preis: number;
  anfangsstand?: number | null;
  endstand?: number | null;
  verbrauch?: number | null;
  anfangsablesung?: string | null;
  endablesung?: string | null;
  abrechnungstage?: number | null;
  kosten: number;
}
