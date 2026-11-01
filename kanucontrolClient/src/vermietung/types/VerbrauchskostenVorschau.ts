import type { Verbrauchsart } from "@/vermietung/enums/Verbrauchsart";
import type { Verbrauchsabrechnungsart } from "@/vermietung/enums/Verbrauchsabrechnungsart";

export interface VerbrauchskostenVorschau {
  zaehlerId: number | null;
  zaehlerBezeichnung: string | null;
  bezeichnung: string;
  verbrauchsart: Verbrauchsart | null;
  abrechnungsart: Verbrauchsabrechnungsart | null;
  mengeneinheit: string | null;
  preis: number | null;
  anfangsstand: number | null;
  endstand: number | null;
  verbrauch: number | null;
  anfangsablesung: string | null;
  endablesung: string | null;
  abrechnungstage: number | null;
  kosten: number | null;
  erfolgreich: boolean;
  hinweis: string | null;
}
