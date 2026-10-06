// src/vermietung/api/mietobjektApi.ts

import apiClient from "@/core/api/client/apiClient";
import type { Mietobjekt } from "../types/Mietobjekt";
import type { MietobjektSave } from "../types/MietobjektSave";

const BASE_URL = "/vermietung/objekte";

export async function getMietobjekte(): Promise<Mietobjekt[]> {
  const response = await apiClient.get<Mietobjekt[]>(BASE_URL);
  return response.data;
}

export async function getMietobjekt(id: number): Promise<Mietobjekt> {
  const response = await apiClient.get<Mietobjekt>(`${BASE_URL}/${id}`);
  return response.data;
}

export async function createMietobjekt(mietobjekt: MietobjektSave): Promise<Mietobjekt> {
  const response = await apiClient.post<Mietobjekt>(BASE_URL, mietobjekt);
  return response.data;
}

export async function updateMietobjekt(
  id: number,
  mietobjekt: MietobjektSave,
): Promise<Mietobjekt> {
  const response = await apiClient.put<Mietobjekt>(`${BASE_URL}/${id}`, mietobjekt);
  return response.data;
}

export async function setMietobjektAktiv(id: number): Promise<Mietobjekt> {
  const response = await apiClient.put<Mietobjekt>(`${BASE_URL}/${id}/aktiv`);
  return response.data;
}

export async function deleteMietobjekt(id: number): Promise<void> {
  await apiClient.delete(`${BASE_URL}/${id}`);
}
