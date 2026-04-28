// src/api/foerdersatzApi.ts

import apiClient from "@/api/client/apiClient";

import type { FoerdersatzDTO, FoerdersatzCreateUpdateDTO } from "@/api/types/Foerdersatz";

/* =========================================================
   GET ALL
   ========================================================= */

export async function getFoerdersaetze(): Promise<FoerdersatzDTO[]> {
  const response = await apiClient.get("/foerdersaetze");

  return response.data;
}

/* =========================================================
   GET BY ID
   ========================================================= */

export async function getFoerdersatzById(id: number): Promise<FoerdersatzDTO> {
  const response = await apiClient.get(`/foerdersaetze/${id}`);

  return response.data;
}

/* =========================================================
   CREATE
   ========================================================= */

export async function createFoerdersatz(dto: FoerdersatzCreateUpdateDTO): Promise<FoerdersatzDTO> {
  const response = await apiClient.post("/foerdersaetze", dto);

  return response.data;
}

/* =========================================================
   UPDATE
   ========================================================= */

export async function updateFoerdersatz(
  id: number,
  dto: FoerdersatzCreateUpdateDTO,
): Promise<FoerdersatzDTO> {
  const response = await apiClient.put(`/foerdersaetze/${id}`, dto);

  return response.data;
}

/* =========================================================
   DELETE
   ========================================================= */

export async function deleteFoerdersatz(id: number): Promise<void> {
  await apiClient.delete(`/foerdersaetze/${id}`);
}

/* =========================================================
   GÜLTIG AM
   ========================================================= */

export async function getGueltigerFoerdersatz(typ: string, datum: string): Promise<FoerdersatzDTO> {
  const response = await apiClient.get("/foerdersaetze/gueltig", {
    params: {
      typ,
      datum,
    },
  });

  return response.data;
}
