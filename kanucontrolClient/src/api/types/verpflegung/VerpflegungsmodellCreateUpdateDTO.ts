//api/types/verpflegung/VerpflegungsmodellDTO.ts

export interface VerpflegungsmodellCreateUpdateDTO {
  bezeichnung: string;
  preisProPersonUndTag: number;
  bemerkung?: string;
  aktiv: boolean;
}