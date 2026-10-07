// src/vermietung/api/mietbereichEigenschaftApi.ts

import apiClient from "@/core/api/client/apiClient";
import type { MietbereichEigenschaft } from "@/vermietung/types/MietbereichEigenschaft";
import type { MietbereichEigenschaftSave } from "@/vermietung/types/MietbereichEigenschaftSave";

function baseUrl(mietobjektId: number, mietbereichId: number) {
  return `/vermietung/objekte/${mietobjektId}/bereiche/${mietbereichId}/eigenschaften`;
}

export async function getMietbereichEigenschaften(
  mietobjektId: number,
  mietbereichId: number,
): Promise<MietbereichEigenschaft[]> {
  const response = await apiClient.get<MietbereichEigenschaft[]>(
    baseUrl(mietobjektId, mietbereichId),
  );

  return response.data;
}

export async function getMietbereichEigenschaft(
  mietobjektId: number,
  mietbereichId: number,
  id: number,
): Promise<MietbereichEigenschaft> {
  const response = await apiClient.get<MietbereichEigenschaft>(
    `${baseUrl(mietobjektId, mietbereichId)}/${id}`,
  );

  return response.data;
}

export async function createMietbereichEigenschaft(
  mietobjektId: number,
  mietbereichId: number,
  eigenschaft: MietbereichEigenschaftSave,
): Promise<MietbereichEigenschaft> {
  const response = await apiClient.post<MietbereichEigenschaft>(
    baseUrl(mietobjektId, mietbereichId),
    eigenschaft,
  );

  return response.data;
}

export async function updateMietbereichEigenschaft(
  mietobjektId: number,
  mietbereichId: number,
  id: number,
  eigenschaft: MietbereichEigenschaftSave,
): Promise<MietbereichEigenschaft> {
  const response = await apiClient.put<MietbereichEigenschaft>(
    `${baseUrl(mietobjektId, mietbereichId)}/${id}`,
    eigenschaft,
  );

  return response.data;
}

export async function deleteMietbereichEigenschaft(
  mietobjektId: number,
  mietbereichId: number,
  id: number,
): Promise<void> {
  await apiClient.delete(`${baseUrl(mietobjektId, mietbereichId)}/${id}`);
}
