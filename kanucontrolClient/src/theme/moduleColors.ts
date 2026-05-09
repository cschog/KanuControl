// src/theme/moduleColors.ts

export type ModuleType = "core" | "addon" | "report" | "admin";

export const moduleColors: Record<ModuleType, string> = {
  core: "#2F6FA3", // Kernmodule
  addon: "#2F8F8B", // Zusatzfunktionen
  report: "#6B7280", // Dokumente / Reports
  admin: "#7B3F61", // System / Verwaltung
};

export const moduleHover: Record<ModuleType, string> = {
  core: "#3E82BC",
  addon: "#3FA6A1",
  report: "#4B5563",
  admin: "#91506F",
};
