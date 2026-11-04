import apiClient from "@/core/api/client/apiClient";
import type { FinanzDashboard } from "@/vermietung/types/FinanzDashboard";
export async function getFinanzDashboard(jahr: number, signal?: AbortSignal): Promise<FinanzDashboard> {
  return (await apiClient.get<FinanzDashboard>(`/vermietung/finanz-dashboard/${jahr}`, { signal })).data;
}
export async function saveFinanzDashboardGrenze(jahr: number, umsatzgrenze: number): Promise<FinanzDashboard> {
  return (await apiClient.put<FinanzDashboard>(`/vermietung/finanz-dashboard/${jahr}/einstellungen`, { umsatzgrenze })).data;
}
