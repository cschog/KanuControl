export interface TeilnehmerList {
  id: number;
  personId: number;

  person: {
    id: number;
    name: string;
    vorname: string;
    hauptvereinAbk?: string | null; // ⭐ NEU
  };

  rolle?: "L" | "M" | null;
}
