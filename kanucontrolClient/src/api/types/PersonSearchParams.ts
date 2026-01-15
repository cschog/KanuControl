// src/api/types/PersonSearchParams.ts
import { Sex } from "@/api/enums/Sex";

export interface PersonSearchParams {
    name?: string;
    vorname?: string;
    sex?: Sex;
    aktiv?: boolean;
    vereinId?: number;
    alterMin?: number;
    alterMax?: number;
    plz?: string;
    ort?: string;
    page?: number;
    size?: number;
    sort?: string; // z.B. "name,asc"
  }