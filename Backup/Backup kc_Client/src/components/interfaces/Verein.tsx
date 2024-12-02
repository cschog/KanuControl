export interface VereinDTO {
  id?: number; // Optional, as it might not be available for new entries
  name: string; // Name of the Verein
  abk: string; // Abbreviation
  strasse?: string; // Street address (optional)
  plz?: string; // Postal code (optional)
  ort?: string; // City or town (optional)
  telefon?: string; // Phone number (optional)
  bankName?: string; // Name of the bank (optional)
  kontoInhaber?: string; // Account holder's name (optional)
  kiAnschrift?: string; // Account holder's address (optional)
  iban?: string; // IBAN for payments (optional)
  bic?: string; // BIC code for payments (optional)
}