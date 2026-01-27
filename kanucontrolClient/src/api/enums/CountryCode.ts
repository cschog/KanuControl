// src/api/enums/CountryCode.ts

export const COUNTRIES = [
  { code: "DE", label: "Deutschland" },
  { code: "AT", label: "Österreich" },
  { code: "CH", label: "Schweiz" },
  { code: "NL", label: "Niederlande" },
  { code: "BE", label: "Belgien" },
  { code: "FR", label: "Frankreich" },
  { code: "IT", label: "Italien" },
  { code: "ES", label: "Spanien" },
  { code: "PL", label: "Polen" },
  { code: "CZ", label: "Tschechien" },
  { code: "DK", label: "Dänemark" },
] as const;

export type CountryCode = (typeof COUNTRIES)[number]["code"];
