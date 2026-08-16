import { ReferenzObjekt } from "@/api/enums/ReferenzObjekt";

export interface DokumentDTO {
  id: number;
  reihenfolge: number;
  titel?: string;
  originalDateiname: string;
  mimeType: string;
  dateigroesse: number;

  bildBreitePixel?: number;
  bildHoehePixel?: number;

  dokumentBreiteMm?: number;
  dokumentHoeheMm?: number;

  referenzObjekt?: ReferenzObjekt;

  createdAt: string;
}
