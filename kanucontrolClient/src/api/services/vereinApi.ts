import apiClient from "@/api/client/apiClient";
import { Verein } from "@/api/types/Verein";

const BASE = "/verein";

export const getAllVereine = async (): Promise<Verein[]> => {
  const { data } = await apiClient.get<Verein[]>(BASE);
  return data;
};

export const createVerein = async (verein: Verein): Promise<Verein> => {
  const { data } = await apiClient.post<Verein>(BASE, verein);
  return data;
};

export const updateVerein = async (verein: Verein): Promise<Verein> => {
  if (!verein.id) {
    throw new Error("Verein ID is missing");
  }

  const { data } = await apiClient.put<Verein>(
    `${BASE}/${verein.id}`,
    verein
  );

  return data;
};

export const deleteVerein = async (vereinId: number): Promise<void> => {
  await apiClient.delete(`${BASE}/${vereinId}`);
};