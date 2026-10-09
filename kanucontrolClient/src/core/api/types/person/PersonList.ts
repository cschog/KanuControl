import type { DataStatus } from "@/kjfp/types/common/DataStatus";

export interface PersonList {
  id: number;
  vorname: string;
  name: string;
  alter?: number | null;
  sex?: string | null;
  ort?: string | null;
  hauptvereinAbk?: string | null;
  dataStatus?: DataStatus;
}
