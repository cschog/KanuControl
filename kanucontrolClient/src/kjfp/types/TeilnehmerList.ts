import type { DataStatus } from "@/kjfp/types/common/DataStatus";

export interface TeilnehmerList {
  id: number;

  personId: number;

  rolle?: "L" | "M" | null;
  kjfpZugeordnet?: boolean;

  alterBeiBeginn?: number;

  person?: {
    id: number;
    vorname: string;
    name: string;
    hauptvereinAbk?: string;
    sex?: "M" | "W" | "D";
    dataStatus?: DataStatus;
  };
}
