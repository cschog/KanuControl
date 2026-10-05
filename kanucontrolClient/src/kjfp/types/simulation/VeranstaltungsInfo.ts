// api/types/simulation/VeranstaltungsInfo.ts

import { VeranstaltungTyp } from "@/kjfp/api/enums/VeranstaltungTyp";

export interface VeranstaltungsInfo {
  id: number;

  name: string;

  beginnDatum: string;

  endeDatum: string;

  typ: VeranstaltungTyp;

  tage: number;

  naechte: number;

  vereinKikZertifiziert: boolean;

  beitragsstrukturId?: number;
  beitragsstrukturName?: string;

  unterkunftsartId?: number;
  unterkunftsartName?: string;

  verpflegungsmodellId?: number;
  verpflegungsmodellName?: string;
}
