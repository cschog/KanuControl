import apiClient from "@/api/client/apiClient";
import { MitgliedDetail } from "@/api/types/Mitglied";

export const createMitglied = async (
  personId: number,
  vereinId: number,
): Promise<MitgliedDetail> => {
  const { data } = await apiClient.post<MitgliedDetail>("/mitglied", {
    personId,
    vereinId,
  });
  return data;
};
