export interface Verein {
  id?: number; // Assuming `id` is a number
  name: string;
  abk: string;
  strasse: string;
  plz: string;
  ort: string;
  telefon: string;
  bankName: string;
  iban: string;
  kontoInhaber?: number;
}
export default Verein;
