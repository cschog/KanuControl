// api/types/PersonFilter.ts
export interface PersonFilter {
  search?: string; // ⭐ NEU (wichtig!)
  verein?: string;
  aktiv?: boolean;
}
