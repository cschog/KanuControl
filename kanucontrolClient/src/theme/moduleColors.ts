// src/theme/moduleColors.ts

export type ModuleType = "core" | "addon" | "report" | "admin";

export const moduleColors: Record<ModuleType, string> = {
  core: "#2F6FA3", // Grundmodule
  addon: "#2F8F8B", // Ergänzungen
  report: "#6B7280", // Reports

  // Verwaltung

  admin: "#7B3F61",
};

export const moduleHover: Record<ModuleType, string> = {
  core: "#3E82BC",
  addon: "#3FA6A1",
  report: "#4B5563",

  admin: "#91506F",
};
