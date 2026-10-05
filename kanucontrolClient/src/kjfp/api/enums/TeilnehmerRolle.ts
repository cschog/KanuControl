// src/api/enums/TeilnehmerRolle.ts

export const TEILNEHMER_ROLLEN = [
  { code: "L", label: "Leitung" },
  { code: "M", label: "Mitarbeiter" },
] as const;

export type TeilnehmerRolle = (typeof TEILNEHMER_ROLLEN)[number]["code"];
