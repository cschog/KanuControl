import type { Buchungsstatus } from "@/vermietung/enums/Buchungsstatus";

export interface BuchungSave {
  anreise: string;
  abreise: string;

  mietobjektId: number;
  mieterId: number;
  mietbereichIds: number[];

  veranstalterVereinId?: number;

  status?: Buchungsstatus;
}
