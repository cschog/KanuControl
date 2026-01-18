import { Sex } from "@/api/enums/Sex";

export interface Person {
  id?: number;
  name: string;
  vorname: string;
  sex: Sex;
  geburtsdatum: string;
  aktiv: boolean;
  strasse: string;
  plz: string;
  countryCode: string;
  ort: string;
  telefon: string;
  telefonFestnetz: string;
  bankName: string;
  iban: string;
}

export default Person;