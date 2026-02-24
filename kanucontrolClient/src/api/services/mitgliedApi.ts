import apiClient from "@/api/client/apiClient";
import { MitgliedDetail } from "@/api/types/Mitglied";
import { MitgliedFunktion } from "@/api/types/MitgliedFunktion";

/* =========================================================
   READ
   ========================================================= */

export const getMitgliederByPerson = async (personId: number): Promise<MitgliedDetail[]> => {
  const { data } = await apiClient.get(`/mitglied/person/${personId}`);
  return data;
};

/* =========================================================
   CREATE
   ========================================================= */

export const createMitglied = async (
  personId: number,
  vereinId: number,
  funktion?: MitgliedFunktion,
): Promise<MitgliedDetail> => {
  const { data } = await apiClient.post(`/mitglied`, {
    personId,
    vereinId,
    funktion,
  });
  return data;
};

/* =========================================================
   UPDATE (nur Funktion)
   ========================================================= */

export const updateMitgliedFunktion = async (
  id: number,
  funktion?: MitgliedFunktion,
): Promise<MitgliedDetail> => {
  const { data } = await apiClient.put(`/mitglied/${id}`, {
    funktion,
  });
  return data;
};

/* =========================================================
   DELETE
   ========================================================= */

export const deleteMitglied = async (id: number): Promise<void> => {
  await apiClient.delete(`/mitglied/${id}`);
};

/* =========================================================
   HAUPTVEREIN
   ========================================================= */

export const setHauptverein = async (id: number): Promise<void> => {
  await apiClient.put(`/mitglied/${id}/hauptverein`);
};
