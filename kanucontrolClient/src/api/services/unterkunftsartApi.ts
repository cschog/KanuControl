// src/api/services/unterkunftsartApi.ts

import apiClient from "@/api/client/apiClient";

import {
  UnterkunftsartDTO,
} from "@/api/types/unterkunft/UnterkunftsartDTO";

import {
  UnterkunftsartCreateUpdateDTO,
} from "@/api/types/unterkunft/UnterkunftsartCreateUpdateDTO";

import {
  UnterkunftsartRef,
} from "@/api/types/unterkunft/UnterkunftsartRef";

/* =========================================================
   GET ALL
   ========================================================= */

export const getUnterkunftsarten = async (): Promise<UnterkunftsartDTO[]> => {
  const response = await apiClient.get<UnterkunftsartDTO[]>("/unterkunftsarten");

  return response.data;
};

export const getUnterkunftsartRefs = async (): Promise<UnterkunftsartRef[]> => {
  const response = await apiClient.get<UnterkunftsartRef[]>(
    "/unterkunftsarten/refs",
  );

  return response.data;
};

/* =========================================================
   CREATE
   ========================================================= */

export const createUnterkunftsart = async (
  dto: UnterkunftsartCreateUpdateDTO,
): Promise<UnterkunftsartDTO> => {
  const response = await apiClient.post<UnterkunftsartDTO>(
    "/unterkunftsarten",
    dto,
  );

  return response.data;
};

/* =========================================================
   UPDATE
   ========================================================= */

export const updateUnterkunftsart = async (
  id: number,
  dto: UnterkunftsartCreateUpdateDTO,
): Promise<UnterkunftsartDTO> => {
  const response = await apiClient.put<UnterkunftsartDTO>(
    `/unterkunftsarten/${id}`,
    dto,
  );

  return response.data;
};

/* =========================================================
   DELETE
   ========================================================= */

export const deleteUnterkunftsart = async (
  id: number,
): Promise<void> => {
  await apiClient.delete(`/unterkunftsarten/${id}`);
};