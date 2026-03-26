import { FinanzKategorie } from "@/api/types/finanz";

export interface Buchung {
  id: number;
  kategorie: FinanzKategorie;
  betrag: number;
  datum: string;
  beschreibung?: string;
}

export interface BuchungCreate {
  kategorie: FinanzKategorie;
  betrag: number;
  datum: string;
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
  deckung: number;
  teilnehmerKostenProPerson: number;
  empfohlenerTeilnehmerBeitrag: number;
}

export interface BelegCreate {
  kuerzel: string;
  datum: string;
  beschreibung?: string;
}
