import apiClient from "@/core/api/client/apiClient";

import type { Buchung } from "@/vermietung/types/Buchung";
import type { BuchungSave } from "@/vermietung/types/BuchungSave";
import type { Buchungsquelle } from "@/vermietung/enums/Buchungsquelle";
import type { Buchungsstatus } from "@/vermietung/enums/Buchungsstatus";

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

export async function getPreis(
  mietbereichId: number,
  buchungsquelle: Buchungsquelle,
  stichtag: string,
): Promise<number> {
  const response = await apiClient.get<number>(`${baseUrl()}/preis`, {
    params: {
      mietbereichId,
      buchungsquelle,
      stichtag,
    },
  });

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

export async function aendereBuchungsstatus(id: number, status: Buchungsstatus): Promise<Buchung> {
  const response = await apiClient.patch<Buchung>(`${baseUrl()}/${id}/status`, { status });

  return response.data;
}
