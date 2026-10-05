// src/api/services/finanzenApi.ts

import apiClient from "@/core/api/client/apiClient";

import { FinanzenDashboardDTO } from "@/kjfp/types/FinanzenDashboard";

export async function getFinanzenDashboard(veranstaltungId: number): Promise<FinanzenDashboardDTO> {
  const res = await apiClient.get<FinanzenDashboardDTO>(
    `/veranstaltungen/${veranstaltungId}/finanzen/dashboard`,
  );

  return res.data;
}
