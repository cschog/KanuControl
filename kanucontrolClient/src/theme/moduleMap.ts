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
  anmeldung: "report",
  abrechnung: "report",
  teilnehmerliste: "report",
  erhebungsbogen: "report",
  reisekostenausgabe: "report",
};
