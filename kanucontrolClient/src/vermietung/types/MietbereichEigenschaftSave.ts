import type { MietbereichEigenschaftTyp } from "../enums/MietbereichEigenschaftTyp";

export interface MietbereichEigenschaftSave {
  sortierung: number;
  bezeichnung: string;
  typ: MietbereichEigenschaftTyp;
  wert: string;
}
