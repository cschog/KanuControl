import apiClient from "@/core/api/client/apiClient";
import type { PersonRef } from "@/core/api/types/person/PersonRef";
import type { Mieter, MieterSave } from "@/vermietung/types/Mieter";

const baseUrl = "/vermietung/mieter";

export async function getMieter(): Promise<Mieter[]> {
  const response = await apiClient.get<Mieter[]>(baseUrl);
  return response.data;
}

export async function createMieter(payload: MieterSave): Promise<Mieter> {
  const response = await apiClient.post<Mieter>(baseUrl, payload);
  return response.data;
}

export async function updateMieter(id: number, payload: MieterSave): Promise<Mieter> {
  const response = await apiClient.put<Mieter>(`${baseUrl}/${id}`, payload);
  return response.data;
}

export async function deleteMieter(id: number): Promise<void> {
  await apiClient.delete(`${baseUrl}/${id}/endgueltig`);
}

export async function archiveMieter(id: number): Promise<void> {
  await apiClient.put(`${baseUrl}/${id}/archivieren`);
}

export async function searchVermietungPersonen(search?: string): Promise<PersonRef[]> {
  const response = await apiClient.get<PersonRef[]>("/person/vermietung/search/ref", { params: { search } });
  return response.data ?? [];
}

export async function getMieterPersonRef(personId: number): Promise<PersonRef> {
  const response = await apiClient.get<PersonRef>(`${baseUrl}/person/${personId}`);
  return response.data;
}
