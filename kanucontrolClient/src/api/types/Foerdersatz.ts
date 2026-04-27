// src/api/types/Foerdersatz.ts

export type VeranstaltungTyp = "JEM" | "FM" | "BM" | "GV";

/* =========================================================
   DTO
   ========================================================= */

export interface FoerdersatzDTO {
  id: number;

  typ: VeranstaltungTyp;

  gueltigVon: string;
  gueltigBis: string | null;

  foerdersatz: number;
  foerderdeckel: number | null;

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
  foerderdeckel: number | null;

  beschluss: string | null;
}
