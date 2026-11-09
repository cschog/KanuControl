import type { Buchungsquelle } from "@/vermietung/enums/Buchungsquelle";

export interface Mieter {
  id: number;
  personId: number;
  name: string;
  vorname: string;
  email?: string;
  telefon?: string;
  telefonFestnetz?: string;
  strasse?: string;
  plz?: string;
  ort?: string;
  countryCode?: string;
  bankName?: string;
  iban?: string;
  bic?: string;
  aktiv: boolean;
  mietobjekte?: string[];
  buchungen: number;
  buchungsquellen: Buchungsquelle[];
}

export type MieterSave = Omit<Mieter, "id" | "personId" | "buchungen" | "buchungsquellen" | "mietobjekte"> & { personId?: number };
