import { Berechnungsart } from "@/vermietung/enums/Berechnungsart";

export interface Mietbereich {
  id: number;
  mietobjektId: number;

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

  zaehlerIds: number[];
  /** Legacy alias for direct-booking prerequisites. */
  erforderlicheMietbereichIds?: number[];
  erforderlicheMietbereichIdsDirekt?: number[];
  erforderlicheMietbereichIdsAirbnb?: number[];
  /** Components whose inventory is blocked when this area is booked. */
  belegteMietbereichIds?: number[];
}
