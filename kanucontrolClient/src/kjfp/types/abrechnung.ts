import { FinanzKategorie } from "@/kjfp/types/finanz";
import { WithId } from "@/core/components/table/GenericTableTanstack";
import { AbrechnungsStatus } from "@/kjfp/api/enums/AbrechnungsStatus";
import { BuchungsHerkunft } from "@/kjfp/types/BuchungsHerkunft";
import { DokumentDTO } from "@/kjfp/types/dokument";
import { Zahlungsweg } from "@/kjfp/types/beitraege";

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
  fahrkosten: number;
  teilnehmerbeitrag: number;
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

export interface RueckzahlungTeilnehmerbeitragCreate {
  zahlungsnachweisId: number;
  betrag: number;
  beschreibung?: string;
  zahlungsweg: Zahlungsweg;
}
