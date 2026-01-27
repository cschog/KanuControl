// src/api/types/PersonSaveDTO.ts

import { Sex } from "@/api/enums/Sex";
import { CountryCode } from "@/api/enums/CountryCode";
import { MitgliedFunktion } from "@/api/enums/MitgliedFunktion";

export interface PersonSaveDTO {
  vorname: string;
  name: string;
  sex: Sex;
  geburtsdatum?: string;

  telefon?: string;
  telefonFestnetz?: string;

  strasse?: string;
  plz?: string;
  ort?: string;
  countryCode?: CountryCode;

  bankName?: string;
  iban?: string;

  mitgliedschaften: {
    vereinId: number;
    hauptVerein: boolean;
    funktion?: MitgliedFunktion;
  }[];
}
