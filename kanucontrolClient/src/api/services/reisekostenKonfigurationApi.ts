import apiClient from "@/api/client/apiClient";

export interface ReisekostenKonfiguration {
  id: number;
  pkwSatz: number;
  mitfahrerSatz: number;
  anhaengerSatz: number;
  gueltigVon: string;
  gueltigBis: string | null;
}

export interface ReisekostenKonfigurationSaveRequest {
  pkwSatz: number;
  mitfahrerSatz: number;
  anhaengerSatz: number;
  gueltigVon: string;
}

export async function getReisekostenKonfigurationen() {
  const res = await apiClient.get<ReisekostenKonfiguration[]>("/admin/reisekosten-konfiguration");

  return res.data;
}

export async function createReisekostenKonfiguration(request: ReisekostenKonfigurationSaveRequest) {
  const res = await apiClient.post<number>("/admin/reisekosten-konfiguration", request);

  return res.data;
}

export async function getAktuelleReisekostenKonfiguration() {
  const res = await apiClient.get<ReisekostenKonfiguration>(
    "/admin/reisekosten-konfiguration/aktuell",
  );

  return res.data;
}
