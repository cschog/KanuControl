// src/api/services/reisekostenApi.ts

import apiClient from "@/api/client/apiClient";

import { PersonRef } from "@/api/types/person/PersonRef";
import {
  ReisekostenabrechnungListResponse,
  ReisekostenabrechnungDetailResponse,
  ReisekostenabrechnungUpdateRequest,
} from "@/api/types/Reisekostenabrechnung";


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

// reisekostenApi.ts

export async function getVerfuegbareReisekostenPersonen(
  veranstaltungId: number,
): Promise<PersonRef[]> {

  const response = await apiClient.get<PersonRef[]>(
    `/reisekosten/veranstaltung/${veranstaltungId}/personen`,
  );

  return response.data;
}

export async function searchVerfuegbareReisekostenPersonen(
  veranstaltungId: number,
  params: { search?: string },
): Promise<PersonRef[]> {
  const response = await apiClient.get<PersonRef[]>(
    `/reisekosten/veranstaltung/${veranstaltungId}/personen`,
    {
      params: {
        search: params.search,
      },
    },
  );

  return response.data;
}

export async function searchVerfuegbareMitfahrer(
  veranstaltungId: number,
  params: { search?: string },
): Promise<PersonRef[]> {
  const response = await apiClient.get<PersonRef[]>(
    `/reisekosten/veranstaltung/${veranstaltungId}/mitfahrer`,
    {
      params,
    },
  );

  return response.data;
}
export async function updateReisekostenabrechnung(
  id: number,
  request: ReisekostenabrechnungUpdateRequest,
): Promise<void> {
  await apiClient.put(`/reisekosten/${id}`, request);
}

export async function deleteReisekostenabrechnung(id: number): Promise<void> {
  await apiClient.delete(`/reisekosten/${id}`);
}

export async function getReisekostenByVeranstaltung(veranstaltungId: number) {
  const res = await apiClient.get(`/veranstaltungen/${veranstaltungId}/reisekosten`);

  return res.data;
}
