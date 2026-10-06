import apiClient from "@/core/api/client/apiClient";
import type { Mietbereich } from "@/vermietung/types/Mietbereich";
import type { MietbereichSave } from "@/vermietung/types/MietbereichSave";

function baseUrl(mietobjektId: number) {
  return `/vermietung/objekte/${mietobjektId}/bereiche`;
}

export async function getMietbereiche(
  mietobjektId: number
): Promise<Mietbereich[]> {
  const response = await apiClient.get<Mietbereich[]>(
    baseUrl(mietobjektId)
  );

  return response.data;
}

export async function getMietbereich(
  mietobjektId: number,
  id: number
): Promise<Mietbereich> {
  const response = await apiClient.get<Mietbereich>(
    `${baseUrl(mietobjektId)}/${id}`
  );

  return response.data;
}

export async function createMietbereich(
  mietobjektId: number,
  mietbereich: MietbereichSave
): Promise<Mietbereich> {
  const response = await apiClient.post<Mietbereich>(
    baseUrl(mietobjektId),
    mietbereich
  );

  return response.data;
}

export async function updateMietbereich(
  mietobjektId: number,
  id: number,
  mietbereich: MietbereichSave
): Promise<Mietbereich> {
  const response = await apiClient.put<Mietbereich>(
    `${baseUrl(mietobjektId)}/${id}`,
    mietbereich
  );

  return response.data;
}

export async function deleteMietbereich(
  mietobjektId: number,
  id: number
): Promise<void> {
  await apiClient.delete(
    `${baseUrl(mietobjektId)}/${id}`
  );
}