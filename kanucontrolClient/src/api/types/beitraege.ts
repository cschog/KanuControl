// src/api/types/beitraege.ts

import { DokumentDTO } from "@/api/types/dokument";

export type TeilnehmerRolle = "L" | "M" | null;
export type ZahlungsStatus = "ROT" | "GELB" | "GRUEN";
export type BeitragsQuelle = "INDIVIDUELL" | "STRUKTUR" | "STANDARD";
export type Zahlungsweg = "UEBERWEISUNG" | "QUITTUNG";

export interface PersonRefDTO {
  id: number;
  vorname: string;
  name: string;
  hauptvereinAbk?: string;
}

export interface TeilnehmerKurzDTO {
  id: number;
  personId: number;
  vorname: string;
  nachname: string;
}

export interface TeilnehmerListDTO {
  id: number;
  personId: number;
  person: PersonRefDTO;

  rolle?: TeilnehmerRolle;
  alterBeiBeginn?: number;

  individuellerBeitrag?: number;
  beitragsQuelle?: BeitragsQuelle;

  sollBeitrag?: number;
  gezahlterBetrag?: number;
  zahlungsstatus?: ZahlungsStatus;
}

export interface ZahlungsnachweisDetailDTO {
  id: number;
  datum: string;
  betrag: number;
  zahlungsweg?: Zahlungsweg | null;
  finanzGruppeId?: number | null;
  bemerkung?: string | null;
  positionen: ZahlungsPositionDTO[];
  dokumente: DokumentDTO[];
}

export interface ZahlungsnachweisListDTO {
  id: number;
  datum?: string;
  betrag: number;
  zahlungsweg?: Zahlungsweg | null;
  finanzGruppeId?: number | null;
  bemerkung?: string;
  anzahlTeilnehmer: number;
  anzahlDokumente: number;
  rueckzahlung: boolean;
  teilnehmer: TeilnehmerKurzDTO[];
}

export interface ZahlungsnachweisUpdateDTO {
  datum?: string;
  betrag?: number;
  zahlungsweg?: Zahlungsweg | null;
  finanzGruppeId?: number | null;
  bemerkung?: string;
  positionen: ZahlungsPositionDTO[];
}

export interface ZahlungsPositionDTO {
  id: number;
  teilnehmerId: number;
  vorname?: string;
  nachname?: string;
  betrag?: number;
}

export interface TeilnehmerBeitragSummaryDTO {
  anzahlTeilnehmer: number;
  bezahlt: number;
  teilweise: number;
  offen: number;
  sollSumme: number;
  bezahltSumme: number;
  offenSumme: number;
}

export interface TeilnehmerBeitraegeResponseDTO {
  summary: TeilnehmerBeitragSummaryDTO;
  zahlungsnachweise: ZahlungsnachweisListDTO[];
  teilnehmer: TeilnehmerListDTO[];
}

export interface FinanzGruppeZahlungDTO {
  zahlungsnachweisId: number;
  datum: string;
  betrag: number;
  zahlungsweg?: Zahlungsweg | null;
  bemerkung?: string | null;
  anzahlDokumente: number;
}

export interface OffeneUeberzahlungDTO {
  zahlungsnachweisId: number;
  datum: string;
  urspruenglicherBetrag: number;
  zugeordnet: number;
  bereitsZurueckgezahlt: number;
  offeneUeberzahlung: number;
  bemerkung?: string;

  finanzGruppeId: number | null;
  finanzGruppeKuerzel?: string;
}

export interface TeilnehmerOhneKontoDTO {
  teilnehmerId: number;
  vorname: string;
  name: string;
}

export interface UeberzahlungTeilnehmerkontoPruefungDTO {
  ueberzahlung: boolean;
  ueberzahlungsbetrag: number;
  teilnehmerOhneKonto: TeilnehmerOhneKontoDTO[];
  gemeinsameFinanzGruppe: boolean;
  finanzGruppeKuerzel?: string | null;
}