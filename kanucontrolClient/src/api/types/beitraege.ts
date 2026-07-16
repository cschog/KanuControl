export type TeilnehmerRolle = "L" | "M" | null;

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

  rolle: TeilnehmerRolle;

  individuellerBeitrag?: number;

  bezahlt: boolean;

  bezahltAm?: string;

  alterBeiBeginn?: number;

  effektiverBeitrag: number;
}

export interface TeilnehmerBeitraegeResponseDTO {

  teilnehmer: TeilnehmerListDTO[];
}
