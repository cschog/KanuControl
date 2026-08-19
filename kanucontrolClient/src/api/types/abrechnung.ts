import { FinanzKategorie } from "@/api/types/finanz";
import { WithId } from "@/components/common/GenericTableTanstack";
import { AbrechnungsStatus } from "@/api/enums/AbrechnungsStatus";
import { BuchungsHerkunft } from "@/api/types/BuchungsHerkunft";
import { DokumentDTO } from "@/api/types/dokument";

export interface AbrechnungBeleg {
  id: number;

  belegnummer: string;
  externeBelegnummer?: string;
  aussteller?: string;

  kuerzel: string;
  datum: string;
  beschreibung?: string;

  herkunft: BuchungsHerkunft;

  dokumente: DokumentDTO[];
  positionen: Buchung[];
}

export interface Buchung extends WithId {
  kategorie: FinanzKategorie;
  betrag: number;
  beschreibung?: string;
  herkunft: BuchungsHerkunft;
}

export interface BuchungCreate {
  kategorie: FinanzKategorie;
  betrag: number;
  beschreibung?: string;
}

export interface AbrechnungDetail {
  veranstaltungId: number;
  status: AbrechnungsStatus;
  belege: AbrechnungBeleg[];
  finanz: FinanzSummary;
  verwendeterFoerdersatz: number;
}

export interface FinanzSummary {
  kosten: number;
  einnahmen: number;
  saldo: number;
  kjfpZuschuss: number;
  deckung: number;
  teilnehmerKostenProPerson: number;
  empfohlenerTeilnehmerBeitrag: number;
  fahrtkosten: number;
}

export interface BelegCreate {
  kuerzel: string;

  datum: string;

  aussteller?: string;
  externeBelegnummer?: string;
  beschreibung?: string;
}

export interface BelegUpdate {
  kuerzel: string;

  datum: string;

  aussteller?: string;
  externeBelegnummer?: string;
  beschreibung?: string;
}
