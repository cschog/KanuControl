// src/api/types/FinanzenDashboard.ts

export interface BetragPositionDTO {
  name: string;
  betrag: number;
}

export interface FoerderungDashboardDTO {
  foerderfaehigeTeilnehmer: number;
  foerdertage: number;

  foerdersatz: number;

  kikZuschlag: number;

  deckel: number;

  gesamtfoerderung: number;
}

export interface FinanzenDashboardDTO {
  planKosten: number;

  istKosten: number;

  planEinnahmen: number;

  istEinnahmen: number;

  planSaldo: number;

  istSaldo: number;

  foerderung: FoerderungDashboardDTO;

  kostenNachKategorie: BetragPositionDTO[];

  einnahmenNachKategorie: BetragPositionDTO[];
}
