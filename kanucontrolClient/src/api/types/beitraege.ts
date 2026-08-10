export type TeilnehmerRolle = "L" | "M" | null;

export type ZahlungsStatus = "ROT" | "GELB" | "GRUEN";

export type BeitragsQuelle = "INDIVIDUELL" | "STRUKTUR" | "STANDARD";

export interface PersonRefDTO {
  id: number;
  vorname: string;
  name: string;
  hauptvereinAbk?: string;
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
  bemerkung?: string | null;
  positionen: ZahlungsPositionDTO[];
  dokumente: ZahlungsnachweisDokumentDTO[];
}

export interface ZahlungsnachweisListDTO {
  id: number;
  datum?: string;
  betrag: number;
  bemerkung?: string;
  anzahlTeilnehmer: number;
  anzahlDokumente: number;
}

export interface ZahlungsnachweisUpdateDTO {
  datum?: string;
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

export interface ZahlungsnachweisDokumentDTO {
  id: number;
  reihenfolge: number;
  titel?: string;
  originalDateiname: string;
  mimeType: string;
  dateigroesse: number;
  createdAt: string;
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
