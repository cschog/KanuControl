export interface Bankverbindung {
  kontoinhaber?: string | null;
  bankName?: string | null;
  iban?: string | null;
  bic?: string | null;
}

export type BankverbindungAuswahl = "PRIMAER" | "ALTERNATIV";
