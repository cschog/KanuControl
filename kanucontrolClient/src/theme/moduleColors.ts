// src/theme/moduleColors.ts

export type ModuleType = "core" | "addon" | "report" | "admin";

export const moduleColors: Record<ModuleType, string> = {
  core: "#1E5AA8", // KC Blau (Control)
  addon: "#D62839", // KC Rot (Kanu)
  report: "#8C8C8C", // neutrales Grau
  admin: "#3A3A3A", // dunkles Anthrazit
};

export const moduleHover: Record<ModuleType, string> = {
  core: "#2B6FC7",
  addon: "#E03E4E",
  report: "#A3A3A3",
  admin: "#505050",
};
