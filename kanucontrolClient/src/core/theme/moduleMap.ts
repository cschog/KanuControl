// src/theme/moduleMap.ts

import { ModuleType } from "./moduleColors";

export const moduleTypeMap: Record<string, ModuleType> = {
  // Grundmodule
  vereine: "core",
  mitglieder: "core",
  veranstaltungen: "core",
  teilnehmer: "core",

  // Ergänzungen
  finanzen: "addon",
  reisekosten: "addon",

  // Reports
  dokumente: "report",

  // Verwaltung
  verwaltung: "admin",

  // System
  admin: "system",
  administration: "system",

  // VERMIETUNG
  mieter: "vermietungBlue",
  mietobjekte: "vermietungBlue",
  buchung: "vermietungYellow",
  abrechnung: "vermietungYellow",
  vermietungVerwaltung: "vermietungDark",
};
