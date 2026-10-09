import type { Buchungsquelle } from "../enums/Buchungsquelle";

export interface Mietpreis {
  id: number;
  mietbereichId: number;
  mietbereichBezeichnung: string;
  buchungsquelle: Buchungsquelle;
  gueltigAb: string;
  preis: number;
  bemerkung?: string;
}
