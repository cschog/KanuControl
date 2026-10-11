import { Berechnungsart } from "@/vermietung/enums/Berechnungsart";

export interface Mietbereich {
  id: number;
  mietobjektId: number;
  zaehlerId: number | null;

  bezeichnung: string;
  sortierung: number;
  beschreibung?: string;

  mietbar: boolean;
  direktbuchungAktiv: boolean;
  airbnbAktiv: boolean;
  airbnbInserat: boolean;

  bestand: number;
  mengeneinheit?: string;

  berechnungsart: Berechnungsart;
}
