export interface Verein {
  id?: number; // Assuming `id` is a number
  name: string;
  abk: string;
  strasse: string;
  plz: string;
  ort: string;
  telefon: string;
  bankName: string;
  kontoInhaber: string;
  kiAnschrift: string;
  iban: string;
}
export default Verein;
