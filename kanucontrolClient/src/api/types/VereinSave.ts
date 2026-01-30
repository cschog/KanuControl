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
  kontoinhaberId?: number; // 👈 genau das, was das Backend braucht
}
