// api/types/MitgliedDTO.ts
export interface MitgliedDTO {
  id: number;
  vereinId: number;
  vereinName: string;
  vereinAbk: string;
  hauptVerein: boolean;
  funktion?: string,
}
