// src/api/types/PersonDetail.ts

import { Sex } from "@/api/enums/Sex";
import { CountryCode } from "@/api/enums/CountryCode";
import { MitgliedDetail } from "./MitgliedDetail";

export interface PersonDetail {
  id?: number;

  vorname: string;
  name: string;
  sex: Sex;
  aktiv: boolean;
  geburtsdatum?: string;

  telefon?: string;
  telefonFestnetz?: string;

  strasse?: string;
  plz?: string;
  ort?: string;
  countryCode?: CountryCode;

  bankName?: string;
  iban?: string;

  mitgliedschaften: MitgliedDetail[];
}
