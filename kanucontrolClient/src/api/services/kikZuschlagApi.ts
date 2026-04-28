// src/api/services/kikZuschlagApi.ts

import apiClient from "@/api/client/apiClient";

import type { KikDTO, KikCreateUpdateDTO } from "@/api/types/Kik";

/* =========================================================
   GET ALL
   ========================================================= */

export async function getKikZuschlag(): Promise<KikDTO[]> {
  const response = await apiClient.get("/kikZuschlag");

  return response.data;
}

/* =========================================================
   GET BY ID
   ========================================================= */

export async function getKikZuschlagById(id: number): Promise<KikDTO> {
  const response = await apiClient.get(`/kikZuschlag/${id}`);

  return response.data;
}

/* =========================================================
   CREATE
   ========================================================= */

export async function createKikZuschlag(dto: KikCreateUpdateDTO): Promise<KikDTO> {
  const response = await apiClient.post("/kikZuschlag", dto);

  return response.data;
}

/* =========================================================
   UPDATE
   ========================================================= */

export async function updateKikZuschlag(
  id: number,
  dto: KikCreateUpdateDTO,
): Promise<KikDTO> {
  const response = await apiClient.put(`/kikZuschlag/${id}`, dto);

  return response.data;
}

/* =========================================================
   DELETE
   ========================================================= */

export async function deleteKikZuschlag(id: number): Promise<void> {
  await apiClient.delete(`/kikZuschlag/${id}`);
}

/* =========================================================
   GÜLTIG AM
   ========================================================= */

export async function getGueltigerKikZuschlag(typ: string, datum: string): Promise<KikDTO> {
  const response = await apiClient.get("/kikZuschlag/gueltig", {
    params: {
      typ,
      datum,
    },
  });

  return response.data;
}
