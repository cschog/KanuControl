// src/theme/moduleColors.ts

export type ModuleType =
  | "core"
  | "addon"
  | "report"
  | "admin"
  | "system"
  | "vermietungBlue"
  | "vermietungYellow"
  | "vermietungDark";

export const moduleColors: Record<ModuleType, string> = {
  // KC
  core: "#1E5AA8", // KC Blau
  addon: "#D62839", // KC Rot
  report: "#8C8C8C", // Grau
  admin: "#3A3A3A", // Anthrazit
  system: "#2E7D32", // Grün

  // VERMIETUNG – EKC Wimpel
  vermietungBlue: "#0057A8", // Wimpel-Blau
  vermietungYellow: "#F2C300", // Wimpel-Gelb
  vermietungDark: "#222222", // Wimpel-Schwarz
};

export const moduleHover: Record<ModuleType, string> = {
  // KC
  core: "#2B6FC7",
  addon: "#E03E4E",
  report: "#A3A3A3",
  admin: "#505050",
  system: "#388E3C",

  // VERMIETUNG
  vermietungBlue: "#006CCB",
  vermietungYellow: "#FFD633",
  vermietungDark: "#3A3A3A",
};
