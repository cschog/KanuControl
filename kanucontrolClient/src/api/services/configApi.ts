// configApi.ts

import apiClient from "@/api/client/apiClient";

export async function getFoerderdeckel(): Promise<number> {
  const res = await apiClient.get<number>("/config/foerderdeckel");
  return res.data;
}
