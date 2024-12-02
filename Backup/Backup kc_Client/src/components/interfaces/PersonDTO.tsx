export interface PersonDTO {
  id?: number; // Optional for new entities
  name: string;
  vorname: string;
  strasse: string;
  plz: string;
  ort: string;
  telefon: string;
  bankName?: string;
  iban?: string;
  bic?: string;
}