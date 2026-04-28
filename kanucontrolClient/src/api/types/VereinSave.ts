// VereinSave.ts
export interface VereinSave {
  id?: number;
  name: string;
  abk: string;
  strasse?: string;
  plz?: string;
  ort?: string;
  telefon?: string;
  bankName?: string;
  iban?: string;
  bic?: string;
  schutzkonzept?: string; // ISO Date
  kikZertifiziertSeit?: string;
  kikZertifiziertBis?: string;
  kontoinhaberId?: number;
}
