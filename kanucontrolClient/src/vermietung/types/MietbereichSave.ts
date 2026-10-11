import { Berechnungsart } from "@/vermietung/enums/Berechnungsart";

export interface MietbereichSave {
  bezeichnung: string;
  sortierung: number;

  beschreibung: string;

  mietbar: boolean;
  direktbuchungAktiv: boolean;
  airbnbAktiv: boolean;
  airbnbInserat: boolean;

  bestand: number;
  mengeneinheit: string;

  berechnungsart: Berechnungsart;

  zaehlerId: number | null;
}
