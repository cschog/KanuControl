// src/api/services/reisekostenApi.ts

import apiClient from "@/api/client/apiClient";

import { ReisekostenabrechnungListResponse } from "@/api/types/Reisekostenabrechnung";
import { ReisekostenabrechnungDetailResponse } from "@/api/types/Reisekostenabrechnung";

export interface ReisekostenabrechnungCreateRequest {
  veranstaltungId: number;
  fahrerId: number;
  abrechnungsdatum: string;
  bemerkung?: string;
}

export async function getReisekostenabrechnungenByVeranstaltung(
  veranstaltungId: number,
): Promise<ReisekostenabrechnungListResponse[]> {
  const response = await apiClient.get<ReisekostenabrechnungListResponse[]>(
    `/reisekosten/veranstaltung/${veranstaltungId}`,
  );

  return response.data;
}

export async function createReisekostenabrechnung(
  dto: ReisekostenabrechnungCreateRequest,
): Promise<number> {
  const response = await apiClient.post<number>(
    "/reisekosten",

    dto,
  );

  return response.data;
}
export async function getReisekostenabrechnung(
  id: number,
): Promise<ReisekostenabrechnungDetailResponse> {
  const response = await apiClient.get<ReisekostenabrechnungDetailResponse>(`/reisekosten/${id}`);

  return response.data;
}
