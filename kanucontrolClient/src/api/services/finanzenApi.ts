// src/api/services/finanzenApi.ts

import apiClient from "@/api/client/apiClient";

import { FinanzenDashboardDTO } from "@/api/types/FinanzenDashboard";

export async function getFinanzenDashboard(veranstaltungId: number): Promise<FinanzenDashboardDTO> {
  const res = await apiClient.get<FinanzenDashboardDTO>(
    `/veranstaltungen/${veranstaltungId}/finanzen/dashboard`,
  );

  return res.data;
}
