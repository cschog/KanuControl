// api/types/Person.ts
import { Sex } from "@/kjfp/api/enums/Sex";
import { CountryCode } from "@/kjfp/api/enums/CountryCode";
import { MitgliedDetail, MitgliedSaveInPerson } from "@/kjfp/types/Mitglied";
import type { DataStatus, DataFieldStatus } from "@/kjfp/types/common/DataStatus";

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
  sex?: "M" | "W" | "D";

  // neu
  dataStatus?: DataStatus;
}

/* ============================
 * DETAIL
 * ============================ */

export interface PersonDataStatus {
  status: DataStatus;
  fields: Record<string, DataFieldStatus>;
}

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
  bic?: string;

  efz?: string;

  mitgliedschaften: MitgliedDetail[];

  // Kontextbezogener Datenstatus

  dataStatus?: PersonDataStatus;
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

  strasse?: string;
  plz?: string;
  ort?: string;
  countryCode?: CountryCode;

  telefon?: string;
  telefonFestnetz?: string;
  bankName?: string;
  iban?: string;
  bic?: string;
  efz?: string; // ISO Date

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
