import type { Buchungsstatus } from "@/vermietung/enums/Buchungsstatus";
import type { Buchungsquelle } from "@/vermietung/enums/Buchungsquelle";

export interface BuchungPosition {
  mietbereichId: number;
  anzahl: number;
}

export interface Buchung {
  id: number;
  buchungsnummer: string;
  status: Buchungsstatus;
  buchungsquelle: Buchungsquelle;

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
}
