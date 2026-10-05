import { Buchung } from "@/kjfp/types/abrechnung";
import { BuchungsHerkunft } from "@/kjfp/types/BuchungsHerkunft";

export function istManuelleBuchung(buchung: Buchung): boolean {
  return buchung.herkunft === BuchungsHerkunft.MANUELL;
}

export function istSystemBuchung(buchung: Buchung): boolean {
  return !istManuelleBuchung(buchung);
}

export function istEditierbar(buchung: Buchung): boolean {
  return istManuelleBuchung(buchung);
}
