import apiClient from "@/core/api/client/apiClient";
import type { VerbrauchDashboard } from "@/vermietung/types/VerbrauchDashboard";
export async function getVerbrauchDashboard(objektId: number, jahr: number, signal?: AbortSignal): Promise<VerbrauchDashboard> {
  return (await apiClient.get<VerbrauchDashboard>(`/vermietung/verbrauch-dashboard/${objektId}/${jahr}`, { signal })).data;
}
