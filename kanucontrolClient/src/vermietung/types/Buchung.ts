import type { Buchungsstatus } from "@/vermietung/enums/Buchungsstatus";
import type { Buchungsquelle } from "@/vermietung/enums/Buchungsquelle";
import { Berechnungsart } from "@/vermietung/enums/Berechnungsart";
import type { BuchungVerbrauchskosten } from "@/vermietung/types/BuchungVerbrauchskosten";

export interface BuchungPosition {
  mietbereichId: number;
  anzahl: number;
  vereinbarterPreis?: number;
  berechnungsart?: Berechnungsart;
}

export interface Buchung {
  id: number;
  buchungsnummer: string;
  status: Buchungsstatus;
  buchungsquelle: Buchungsquelle;

  createdAt: string;

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

  verbrauchskosten?: BuchungVerbrauchskosten[];
}
