import apiClient from "@/core/api/client/apiClient";
import { MitgliedDetail } from "@/kjfp/types/Mitglied";

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
