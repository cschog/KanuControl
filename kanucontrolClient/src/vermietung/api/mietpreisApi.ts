import apiClient from "@/core/api/client/apiClient";

import type { Mietpreis } from "@/vermietung/types/Mietpreis";
import type { MietpreisSave } from "@/vermietung/types/MietpreisSave";

function baseUrl() {
  return "/vermietung/mietpreise";
}

export async function getMietpreise(mietbereichId: number): Promise<Mietpreis[]> {
  const response = await apiClient.get<Mietpreis[]>(`${baseUrl()}/mietbereich/${mietbereichId}`);

  return response.data;
}

export async function createMietpreis(mietpreis: MietpreisSave): Promise<Mietpreis> {
  const response = await apiClient.post<Mietpreis>(baseUrl(), mietpreis);

  return response.data;
}

export async function updateMietpreis(id: number, mietpreis: MietpreisSave): Promise<Mietpreis> {
  const response = await apiClient.put<Mietpreis>(`${baseUrl()}/${id}`, mietpreis);

  return response.data;
}
