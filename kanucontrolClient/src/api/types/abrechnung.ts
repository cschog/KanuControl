import { FinanzKategorie } from "@/api/types/finanz";
import { WithId } from "@/components/common/GenericTableTanstack";

export interface Buchung extends WithId {
  kategorie: FinanzKategorie;
  betrag: number;
  beschreibung?: string;
  systemGenerated?: boolean;
}

export interface BuchungCreate {
  kategorie: FinanzKategorie;
  betrag: number;
  beschreibung?: string;
}

export interface AbrechnungBeleg {
  id: number;
  belegnummer: string;
  datum: string;
  beschreibung?: string;
  kuerzel: string;
  positionen: Buchung[];
}

export interface AbrechnungDetail {
  veranstaltungId: number;
  status: "OFFEN" | "ABGESCHLOSSEN";
  belege: AbrechnungBeleg[];
  finanz: FinanzSummary;
  verwendeterFoerdersatz: number;
}

export interface FinanzSummary {
  kosten: number;
  einnahmen: number;
  saldo: number;
  kjfpZuschuss: number
  deckung: number;
  teilnehmerKostenProPerson: number;
  empfohlenerTeilnehmerBeitrag: number;
}

export interface BelegCreate {
  kuerzel: string;
  datum: string;
  beschreibung?: string;
}

export interface BelegUpdate {
  kuerzel: string;
  datum: string;
  beschreibung?: string;
}
