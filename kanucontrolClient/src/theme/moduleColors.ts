// src/theme/moduleColors.ts

export type ModuleType = "core" | "addon" | "report";

export const moduleColors: Record<ModuleType, string> = {
  core: "#2F6FA3", // Grundmodule
  addon: "#2F8F8B", // Ergänzungen
  report: "#6B7280", // Reports
};

export const moduleHover: Record<ModuleType, string> = {
  core: "#3E82BC",
  addon: "#3FA6A1",
  report: "#4B5563",
};
