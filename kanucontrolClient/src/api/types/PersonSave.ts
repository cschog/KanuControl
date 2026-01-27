// api/types/PersonSave.ts
import { Sex } from "@/api/enums/Sex";

export interface PersonSave {
  id?: number; // ❗ nur bei update
  name: string;
  vorname: string;
  sex: Sex;

  geburtsdatum?: string;
  countryCode?: string;

  strasse?: string;
  plz?: string;
  ort?: string;

  telefon?: string;
  telefonFestnetz?: string;
  bankName?: string;
  iban?: string;

  aktiv?: boolean;
}
