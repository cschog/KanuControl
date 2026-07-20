import { Buchung } from "@/api/types/abrechnung";
import { BuchungsHerkunft } from "@/api/types/BuchungsHerkunft";

export function istManuelleBuchung(buchung: Buchung): boolean {
  return buchung.herkunft === BuchungsHerkunft.MANUELL;
}

export function istSystemBuchung(buchung: Buchung): boolean {
  return !istManuelleBuchung(buchung);
}

export function istEditierbar(buchung: Buchung): boolean {
  return istManuelleBuchung(buchung);
}
