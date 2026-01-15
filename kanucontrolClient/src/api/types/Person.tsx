import { Sex } from "@/api/enums/Sex";

export interface Person {
  id?: number;
  name: string;
  vorname: string;
  sex: Sex;
  strasse: string;
  plz: string;
  ort: string;
  telefon: string;
  bankName: string;
  iban: string;
}

export default Person;