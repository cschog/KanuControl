// src/api/services/verpflegungsmodellApi.ts

import apiClient from "@/api/client/apiClient";

import {
  VerpflegungsmodellDTO,
} from "@/api/types/verpflegung/VerpflegungsmodellDTO";

import {
  VerpflegungsmodellCreateUpdateDTO,
} from "@/api/types/verpflegung/VerpflegungsmodellCreateUpdateDTO";

import {
  VerpflegungsmodellRef,
} from "@/api/types/veranstaltung/VerpflegungsmodellRef";

/* =========================================================
   GET ALL
   ========================================================= */

export const getVerpflegungsmodelle = async (): Promise<VerpflegungsmodellDTO[]> => {
  const response = await apiClient.get<VerpflegungsmodellDTO[]>("/verpflegungsmodelle");

  return response.data;
};

export const getVerpflegungsmodellRefs = async (): Promise<VerpflegungsmodellRef[]> => {
  const response = await apiClient.get<VerpflegungsmodellRef[]>(
    "/verpflegungsmodelle/refs",
  );

  return response.data;
};

/* =========================================================
   CREATE
   ========================================================= */

export const createVerpflegungsmodell = async (
  dto: VerpflegungsmodellCreateUpdateDTO,
): Promise<VerpflegungsmodellDTO> => {
  const response = await apiClient.post<VerpflegungsmodellDTO>(
    "/verpflegungsmodelle",
    dto,
  );

  return response.data;
};

/* =========================================================
   UPDATE
   ========================================================= */

export const updateVerpflegungsmodell = async (
  id: number,
  dto: VerpflegungsmodellCreateUpdateDTO,
): Promise<VerpflegungsmodellDTO> => {
  const response = await apiClient.put<VerpflegungsmodellDTO>(
    `/verpflegungsmodelle/${id}`,
    dto,
  );

  return response.data;
};

/* =========================================================
   DELETE
   ========================================================= */

export const deleteVerpflegungsmodell = async (
  id: number,
): Promise<void> => {
  await apiClient.delete(`/verpflegungsmodelle/${id}`);
};