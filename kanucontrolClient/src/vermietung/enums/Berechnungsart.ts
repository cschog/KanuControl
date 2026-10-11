// src/vermietung/enums/Berechnungsart.ts
export enum Berechnungsart {
  ZEITRAUM = "ZEITRAUM",
  TAG = "TAG",
  BUCHUNG = "BUCHUNG",
  GANZJAEHRIG = "GANZJAEHRIG",
}

export const BERECHNUNGSART_LABELS: Record<Berechnungsart, string> = {
  [Berechnungsart.ZEITRAUM]: "Zeitraum",
  [Berechnungsart.TAG]: "Pro Tag",
  [Berechnungsart.BUCHUNG]: "Pro Buchung",
  [Berechnungsart.GANZJAEHRIG]: "Ganzjährig",
};
