// src/api/auditApi.ts

import api from "@/api/client/apiClient";
import { AuditSession } from "@/api/types/audit";

export async function getActiveSessions(): Promise<AuditSession[]> {
  const response = await api.get("/admin/audit/active-sessions");
  return response.data;
}

export async function getAuditHistory(
  page: number,
  size: number,
) {
  const response = await api.get(`/admin/audit/history?page=${page}&size=${size}`);

  return response.data;
}

export async function getAuditDashboard() {
  const response = await api.get("/admin/audit/dashboard");
  return response.data;
}

