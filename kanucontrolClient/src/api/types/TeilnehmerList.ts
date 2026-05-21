export interface TeilnehmerList {
  id: number;

  personId: number;

  rolle?: "L" | "M" | null;

  alterBeiBeginn?: number;

  person?: {
    id: number;

    vorname: string;

    name: string;

    hauptvereinAbk?: string;
  };
}
