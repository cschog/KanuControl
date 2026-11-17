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

/** Maßgeblich sind die lokalen Aufenthaltstage, auch beim Sommerzeitwechsel. */
export function hatUebernachtung(anreise?: string | null, abreise?: string | null, unbefristet = false): boolean {
  return !unbefristet && !!anreise && !!abreise && abreise.slice(0, 10) > anreise.slice(0, 10);
}

export function mietdauerLabel(einheiten: number, anreise?: string | null, abreise?: string | null, unbefristet = false): string {
  const einheit = hatUebernachtung(anreise, abreise, unbefristet)
    ? einheiten === 1 ? "Übernachtung" : "Übernachtungen"
    : einheiten === 1 ? "Tag" : "Tage";
  return `${einheiten} ${einheit}`;
}

/** Auch ein pauschaler Mietpreis kann einen Aufenthalt mit Übernachtungen abdecken. */
export function mietpositionsDauerLabel(art: string | undefined, einheiten: number, anreise?: string | null, abreise?: string | null, unbefristet = false): string {
  if (art !== "GANZJAEHRIG" && hatUebernachtung(anreise, abreise, unbefristet)) {
    const dauer = mietEinheiten(Berechnungsart.TAG, anreise!, abreise!);
    return mietdauerLabel(dauer, anreise, abreise) + (art !== "TAG" && dauer > 1 ? " (pauschal)" : "");
  }
  return art === "TAG" ? mietdauerLabel(einheiten, anreise, abreise, unbefristet) : "";
}
