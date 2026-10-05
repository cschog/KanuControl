// src/api/types/Foerdersatz.ts

export type VeranstaltungTyp = "JEM" | "FM";

/* =========================================================
   DTO
   ========================================================= */

export interface FoerdersatzDTO {
  id: number;

  typ: VeranstaltungTyp;

  gueltigVon: string;
  gueltigBis: string | null;

  foerdersatz: number;

  beschluss: string | null;
}

/* =========================================================
   CREATE / UPDATE
   ========================================================= */

export interface FoerdersatzCreateUpdateDTO {
  typ: VeranstaltungTyp;

  gueltigVon: string;
  gueltigBis: string | null;

  foerdersatz: number;

  beschluss: string | null;
}
