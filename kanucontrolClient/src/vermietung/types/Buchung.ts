import type { Buchungsstatus } from "@/vermietung/enums/Buchungsstatus";
import type { Buchungsquelle } from "@/vermietung/enums/Buchungsquelle";
import { Berechnungsart } from "@/vermietung/enums/Berechnungsart";
import type { BuchungVerbrauchskosten } from "@/vermietung/types/BuchungVerbrauchskosten";
import type { VerbrauchskostenVorschau } from "./VerbrauchskostenVorschau";
import type { Rabattart } from "@/vermietung/enums/Rabattart";
import type { RabattwertTyp } from "@/vermietung/enums/RabattwertTyp";

export interface BuchungPosition {
  mietbereichId: number;
  mietbereichBezeichnung?: string;
  anzahl: number;
  vereinbarterPreis?: number;
  berechnungsart?: Berechnungsart;
  rabattwertTyp?: RabattwertTyp | null;
  rabattwert?: number | null;
  rabattbetrag?: number;
}

export interface Buchung {
  id: number;
  buchungsnummer: string;
  status: Buchungsstatus;
  buchungsquelle: Buchungsquelle;

  createdAt: string | null;

  anreise: string;
  abreise: string;

  unbefristet: boolean;

  mietobjektId: number;
  mietobjektBezeichnung: string;

  mietbereichIds: number[];
  positionen: BuchungPosition[];

  mieterId: number;
  mieterVorname: string;
  mieterName: string;

  veranstalterVereinId?: number;

  bemerkung?: string | null;
  bewertung?: number | null;

  rabattart: Rabattart;
  rabattwertTyp?: RabattwertTyp | null;
  rabattwert?: number | null;
  rabattbetrag: number;

  verbrauchskosten?: BuchungVerbrauchskosten[];
  verbrauchskostenVorschau: VerbrauchskostenVorschau[];
}
