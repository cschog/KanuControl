// api/types/Person.ts
import { Sex } from "@/api/enums/Sex";
import { CountryCode } from "@/api/enums/CountryCode";
import { MitgliedDetail, MitgliedSaveInPerson } from "@/api/types/Mitglied";

/* ============================
 * LIST
 * ============================ */
export interface PersonList {
  id: number;
  vorname: string;
  name: string;
  alter?: number;
  ort?: string;
  hauptvereinAbk?: string;
  mitgliedschaftenCount: number;
}

/* ============================
 * DETAIL
 * ============================ */
export interface PersonDetail {
  id: number;
  vorname: string;
  name: string;
  sex: Sex;
  aktiv: boolean;
  geburtsdatum?: string;

  email?: string;

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

/* ============================
 * SAVE (Create / Update)
 * ============================ */
export interface PersonSave {
  name: string;
  vorname: string;
  sex: Sex;

  geburtsdatum?: string;
  email?: string;
  countryCode?: CountryCode;

  strasse?: string;
  plz?: string;
  ort?: string;

  telefon?: string;
  telefonFestnetz?: string;
  bankName?: string;
  iban?: string;

  aktiv?: boolean;

  /** Aggregat-Save */
  mitgliedschaften?: MitgliedSaveInPerson[];
}

/* ============================
 * SEARCH
 * ============================ */
export interface PersonSearchParams {
  name?: string;
  vorname?: string;
  sex?: Sex;
  aktiv?: boolean;
  vereinId?: number;
  alterMin?: number;
  alterMax?: number;
  plz?: string;
  ort?: string;
  page?: number;
  size?: number;
  sort?: string;
}