//api/types/unterkunft/UnterkunftsartDTO.ts

export interface UnterkunftsartDTO {
  id: number;
  bezeichnung: string;
  preisProPersonUndNacht: number;
  bemerkung?: string;
  aktiv: boolean;
}