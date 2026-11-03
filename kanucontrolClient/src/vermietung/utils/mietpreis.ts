import { Berechnungsart } from "@/vermietung/enums/Berechnungsart";

/** Kalendertage statt Stunden: 16 Uhr bis 14 Uhr zwei Tage später sind zwei Miettage. */
export function mietEinheiten(art: Berechnungsart | undefined, anreise: string, abreise: string, unbefristet = false): number {
  if (art !== Berechnungsart.TAG || unbefristet || !anreise || !abreise) return 1;
  const datum = (value: string) => {
    const [jahr, monat, tag] = value.slice(0, 10).split("-").map(Number);
    return Date.UTC(jahr, monat - 1, tag);
  };
  const tage = (datum(abreise) - datum(anreise)) / 86400000;
  return Number.isFinite(tage) ? Math.max(1, tage) : 1;
}

export function geldRunden(value: number): number {
  return Math.round((value + Number.EPSILON) * 100) / 100;
}
