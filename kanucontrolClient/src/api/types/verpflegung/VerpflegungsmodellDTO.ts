//api/types/verpflegung/VerpflegungsmodellDTO.ts

export interface VerpflegungsmodellDTO {
  id: number;
  bezeichnung: string;
  preisProPersonUndTag: number;
  bemerkung?: string;
  aktiv: boolean;
}