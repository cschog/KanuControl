import { Sex } from "@/api/enums/Sex";
import { Mitglied } from "@/api/types/Mitglied";

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

  mitglieder?: Mitglied[];
}

export default Person;