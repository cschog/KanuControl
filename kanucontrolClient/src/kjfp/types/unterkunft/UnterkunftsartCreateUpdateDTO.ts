// api/types/untrkunft/UnterkunftsartCreateUpdateDTO.ts

export interface UnterkunftsartCreateUpdateDTO {
  bezeichnung: string;
  preisProPersonUndNacht: number;
  bemerkung?: string;
  aktiv: boolean;
}