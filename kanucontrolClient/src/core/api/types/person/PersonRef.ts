// src/core/api/types/person/PersonRef.ts
export interface PersonRef {
  id: number;
  name: string;
  vorname: string;
  hauptvereinAbk?: string;
  verwendetInFahrtabschnitten?: boolean;
}
