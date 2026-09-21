// api/services/teilnehmerApi.ts

import apiClient from "@/api/client/apiClient";

import { TeilnehmerList } from "@/api/types/TeilnehmerList";

import { mapRoleFromBackend, mapRoleToBackend } from "../mappers/teilnehmerMapper";

/* =========================================================
   TYPES
   ========================================================= */

type TeilnehmerBackend = Omit<TeilnehmerList, "rolle"> & {
  rolle: string | null;
};

/* =========================================================
   AVAILABLE
   ========================================================= */

export async function getAvailablePersons(
  veranstaltungId: number,
  page: number,
  size: number,
  search?: string,
  verein?: string,
  sortField?: string,
  sortDirection?: "asc" | "desc",
  aktiv?: boolean,
) {
  const res = await apiClient.get(
    `/veranstaltungen/${veranstaltungId}/teilnehmer/available/paged`,
    {
      params: {
        page,
        size,
        search,
        verein,
        sortField,
        sortDirection,
        aktiv,
      },
    },
  );

  return res.data;
}

/* =========================================================
   ASSIGNED
   ========================================================= */

export async function getTeilnehmer(
  veranstaltungId: number,
  page: number,
  size: number,
  search?: string,
  verein?: string,
  sortField?: string,
  sortDirection?: "asc" | "desc",
) {
  const res = await apiClient.get<{
    content: TeilnehmerBackend[];
    totalElements: number;
  }>(`/veranstaltungen/${veranstaltungId}/teilnehmer/paged`, {
    params: {
      page,
      size,
      search: search || undefined,
      verein: verein || undefined,
      sortField,
      sortDirection,
    },
  });

  const content: TeilnehmerList[] = res.data.content.map((t) => ({
    ...t,
    rolle: mapRoleFromBackend(t.rolle),
  }));

  return {
    ...res.data,
    content,
  };
}

/* =========================================================
   ADD BULK
   ========================================================= */

export function addTeilnehmerBulk(veranstaltungId: number, personIds: number[]) {
  return apiClient.post(`/veranstaltungen/${veranstaltungId}/teilnehmer/bulk`, {
    personIds,
  });
}

/* =========================================================
   REMOVE BULK
   ========================================================= */

export async function removeTeilnehmerBulk(veranstaltungId: number, personIds: number[]) {
  await apiClient.delete(`/veranstaltungen/${veranstaltungId}/teilnehmer/bulk`, {
    data: {
      personIds,
    },
  });
}

/* =========================================================
   UPDATE ROLE
   ========================================================= */

export async function updateTeilnehmerRolle(
  veranstaltungId: number,
  personId: number,
  rolle: "L" | "M" | null,
) {
  return apiClient.put(`/veranstaltungen/${veranstaltungId}/teilnehmer/${personId}/rolle`, {
    rolle: mapRoleToBackend(rolle),
  });
}

import { Teilnehmer } from "@/api/types/teilnehmer";

/* =========================================================
   SEARCH WITHOUT FINANZGRUPPE
   ========================================================= */

export async function searchTeilnehmer(
  veranstaltungId: number,
  search: string,
): Promise<Teilnehmer[]> {
  const res = await apiClient.get<Teilnehmer[]>(
    `/veranstaltungen/${veranstaltungId}/teilnehmer/search/ohne-finanzgruppe`,
    {
      params: {
        search,
      },
    },
  );

  return res.data ?? [];
}

/* =========================================================
   COUNT
   ========================================================= */

export async function getTeilnehmerCount(veranstaltungId: number): Promise<number> {
  const res = await apiClient.get(`/veranstaltungen/${veranstaltungId}/teilnehmer/count`);

  return res.data;
}

/* =========================================================
   REMOVE FROM FINANZGRUPPE
   ========================================================= */

export async function removeTeilnehmerFromGruppe(
  veranstaltungId: number,
  gruppeId: number,
  personId: number,
) {
  await apiClient.delete(
    `/veranstaltungen/${veranstaltungId}/finanzgruppen/${gruppeId}/teilnehmer/${personId}`,
  );
}

/* =========================================================
   ADD SINGLE
   ========================================================= */

export async function addTeilnehmer(veranstaltungId: number, personId: number) {
  return apiClient.post(`/veranstaltungen/${veranstaltungId}/teilnehmer/${personId}`);
}
