import type { Buchungsstatus } from "@/vermietung/enums/Buchungsstatus";

export interface Buchung {
  id: number;
  buchungsnummer: string;
  status: Buchungsstatus;
  anreise: string;
  abreise: string;
  mietobjektId: number;
  mietobjektBezeichnung: string;
  mietbereichIds: number[];

  mieterId: number;
  mieterVorname: string;
  mieterName: string;
  veranstalterVereinId?: number;
}
