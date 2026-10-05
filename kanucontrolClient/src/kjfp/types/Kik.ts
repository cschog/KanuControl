// src/api/types/Kik.ts

/* =========================================================
   DTO
   ========================================================= */

export interface KikDTO {
  id: number;

  gueltigVon: string;
  gueltigBis: string | null;

  kikZuschlag: number;

  beschluss: string | null;
}

/* =========================================================
   CREATE / UPDATE
   ========================================================= */

export interface KikCreateUpdateDTO {
  gueltigVon: string;
  gueltigBis: string | null;

  kikZuschlag: number;

  beschluss: string | null;
}
