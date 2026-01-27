// api/types/Person.ts
import { Sex } from "@/api/enums/Sex";
import { CountryCode } from "@/api/enums/CountryCode";
import { Mitglied } from "@/api/types/Mitglied";

export interface Person {
  id?: number;

  name: string;
  vorname: string;
  sex: Sex;
  geburtsdatum?: string;
  alter?: number;

  aktiv: boolean;

  strasse?: string;
  plz?: string;
  ort?: string;
  countryCode?: CountryCode;

  telefon?: string;
  telefonFestnetz?: string;
  bankName?: string;
  iban?: string;

  // 🔑 NUR UI / READ
  mitgliedschaften?: Mitglied[];
}
