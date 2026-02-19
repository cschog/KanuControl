// src/api/types/VeranstaltungList.ts
export interface VeranstaltungList {
  id: number;

  name: string;
  typ: string;

  beginnDatum: string;
  endeDatum: string;

  vereinName: string;
  leiterName: string;
  leiterVorname: string;

  aktiv: boolean;
}
