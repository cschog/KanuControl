import apiClient from "@/core/api/client/apiClient";

import type { Buchung } from "@/vermietung/types/Buchung";
import type { BuchungSave } from "@/vermietung/types/BuchungSave";

function baseUrl() {
  return "/vermietung/buchungen";
}

export async function getBuchungen(): Promise<Buchung[]> {
  const response = await apiClient.get<Buchung[]>(baseUrl());

  return response.data;
}

export async function getBuchung(id: number): Promise<Buchung> {
  const response = await apiClient.get<Buchung>(`${baseUrl()}/${id}`);

  return response.data;
}

export async function createBuchung(buchung: BuchungSave): Promise<Buchung> {
  const response = await apiClient.post<Buchung>(baseUrl(), buchung);

  return response.data;
}

export async function updateBuchung(id: number, buchung: BuchungSave): Promise<Buchung> {
  const response = await apiClient.put<Buchung>(`${baseUrl()}/${id}`, buchung);

  return response.data;
}

export async function deleteBuchung(id: number): Promise<void> {
  await apiClient.delete(`${baseUrl()}/${id}`);
}
