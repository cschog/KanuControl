import type { Buchungsstatus } from "@/vermietung/enums/Buchungsstatus";
import type { Buchungsquelle } from "@/vermietung/enums/Buchungsquelle";

export interface BuchungPositionSave {
  mietbereichId: number;
  anzahl: number;
}

export interface BuchungSave {
  anreise: string;
  abreise: string;

  unbefristet?: boolean;

  mietobjektId: number;
  mieterId: number;

  buchungsquelle: Buchungsquelle;

  mietbereichIds: number[];
  positionen: BuchungPositionSave[];

  veranstalterVereinId?: number;
  status?: Buchungsstatus;
}
