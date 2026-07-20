import { AbrechnungBeleg, Buchung } from "@/api/types/abrechnung";
import { BuchungsHerkunft } from "@/api/types/BuchungsHerkunft";
import { kategorieZuTyp } from "@/api/types/finanz";

export function istManuellerBeleg(beleg: AbrechnungBeleg): boolean {
  return beleg.herkunft === BuchungsHerkunft.MANUELL;
}

export function istSystemBeleg(beleg: AbrechnungBeleg): boolean {
  return !istManuellerBeleg(beleg);
}

export function istEditierbarerBeleg(beleg: AbrechnungBeleg): boolean {
  return istManuellerBeleg(beleg);
}

export function berechneBelegsumme(beleg: AbrechnungBeleg): number {
  return beleg.positionen.reduce((sum, p) => {
    return sum + (kategorieZuTyp[p.kategorie] === "KOSTEN" ? -p.betrag : p.betrag);
  }, 0);
}

// buchungUtils.ts

export function istInBeleglisteSichtbar(buchung: Buchung): boolean {
    return buchung.herkunft !== BuchungsHerkunft.KJFP;
}
