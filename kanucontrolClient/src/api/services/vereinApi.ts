import apiClient from "@/api/client/apiClient";
import Verein from "@/api/types/VereinFormModel";
import { VereinSave } from "@/api/types/VereinSave";
import { VereinRef } from "@/api/types/VereinRef";
import { Page } from "@/api/types/Page";

const BASE = "/verein";

export const getAllVereine = async (): Promise<Verein[]> => {
  const { data } = await apiClient.get<Verein[]>(BASE);
  return data;
};

export const createVerein = async (verein: VereinSave): Promise<Verein> => {
  const { data } = await apiClient.post<Verein>(BASE, verein);
  return data;
};

export const updateVerein = async (id: number, payload: VereinSave): Promise<Verein> => {
  const { data } = await apiClient.put<Verein>(`${BASE}/${id}`, payload);
  return data;
};

export const deleteVerein = async (vereinId: number): Promise<void> => {
  await apiClient.delete(`${BASE}/${vereinId}`);
};

export async function searchVereinePage(params: {
  search?: string;
  page?: number;
  size?: number;
}): Promise<Page<VereinRef>> {
  const { data } = await apiClient.get("/verein/search/ref", {
    params,
  });
  return data;
}
