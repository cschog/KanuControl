export default interface VereinDTO {
  id?: number; // Optional because it may not exist for new objects
  name: string;
  abk: string;
  strasse?: string;
  plz?: string;
  ort?: string;
  telefon?: string;
  bankName?: string;
  kontoInhaber?: string;
  kiAnschrift?: string;
  iban?: string;
  bic?: string;
}