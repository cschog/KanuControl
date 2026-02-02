// src/api/types/VereinFormModel.ts

import { PersonRef } from "@/api/types/PersonRef";

export default interface VereinFormModel {
  /** ID – nur bei READ / EDIT */
  id?: number;

  /** Pflichtfelder (Backend: @NotNull) */
  name: string;
  abk: string;

  /** Adresse */
  strasse?: string;
  plz?: string;
  ort?: string;

  /** Kontakt */
  telefon?: string;

  /** Bankdaten */
  bankName?: string;
  iban?: string;

  /** 🔗 Kontoinhaber (Person) */
  kontoinhaber?: PersonRef;

  mitgliederCount?: number;
}
