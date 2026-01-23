import apiClient from "@/api/client/apiClient";
import { MitgliedDTO } from "@/api/types/MitgliedDTO";

export const createMitglied = async (personId: number, vereinId: number): Promise<MitgliedDTO> => {
  const { data } = await apiClient.post<MitgliedDTO>("/mitglied", {
    personId,
    vereinId,
  });

  return data;
};
