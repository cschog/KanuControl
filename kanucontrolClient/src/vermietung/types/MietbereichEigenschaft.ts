import type { MietbereichEigenschaftTyp } from "../enums/MietbereichEigenschaftTyp";

export interface MietbereichEigenschaft {
  id: number;
  mietbereichId: number;
  sortierung: number;
  bezeichnung: string;
  typ: MietbereichEigenschaftTyp;
  wert: string;
}
