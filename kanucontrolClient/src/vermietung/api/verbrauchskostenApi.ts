import apiClient from "@/core/api/client/apiClient";

import type { Verbrauchskosten } from "@/vermietung/types/Verbrauchskosten";
import type { VerbrauchskostenSave } from "@/vermietung/types/VerbrauchskostenSave";

const basePath = (mietobjektId: number) => `/vermietung/objekte/${mietobjektId}/verbrauchskosten`;

export async function getVerbrauchskosten(mietobjektId: number): Promise<Verbrauchskosten[]> {
  const response = await apiClient.get<Verbrauchskosten[]>(basePath(mietobjektId));

  return response.data;
}

export async function getVerbrauchskostenById(
  mietobjektId: number,
  id: number,
): Promise<Verbrauchskosten> {
  const response = await apiClient.get<Verbrauchskosten>(`${basePath(mietobjektId)}/${id}`);

  return response.data;
}

export async function createVerbrauchskosten(
  mietobjektId: number,
  daten: VerbrauchskostenSave,
): Promise<Verbrauchskosten> {
  const response = await apiClient.post<Verbrauchskosten>(basePath(mietobjektId), daten);

  return response.data;
}

export async function updateVerbrauchskosten(
  mietobjektId: number,
  id: number,
  daten: VerbrauchskostenSave,
): Promise<Verbrauchskosten> {
  const response = await apiClient.put<Verbrauchskosten>(`${basePath(mietobjektId)}/${id}`, daten);

  return response.data;
}

export async function deleteVerbrauchskosten(mietobjektId: number, id: number): Promise<void> {
  await apiClient.delete(`${basePath(mietobjektId)}/${id}`);
}
