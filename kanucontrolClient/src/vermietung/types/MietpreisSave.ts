import type { Buchungsquelle } from "../enums/Buchungsquelle";

export interface MietpreisSave {
  mietbereichId: number;
  buchungsquelle: Buchungsquelle;
  gueltigAb: string;
  preis: number;
  bemerkung?: string;
}
