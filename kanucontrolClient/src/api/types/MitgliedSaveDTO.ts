// src/api/types/MitgliedSaveDTO.ts

export interface MitgliedSaveDTO {
  vereinId: number;
  hauptVerein: boolean;
  funktion?: string | null;
}
