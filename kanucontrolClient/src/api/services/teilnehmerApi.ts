// api/services/teilnehmerApi.ts

import apiClient from "@/api/client/apiClient";
import { TeilnehmerList } from "@/api/types/TeilnehmerList";
import { mapRoleFromBackend, mapRoleToBackend } from "../mappers/teilnehmerMapper";

/* ================= AVAILABLE ================= */

export async function getAvailablePersons(
  veranstaltungId: number,
  page: number,
  size: number,
  search?: string,
  verein?: string,
) {
  const res = await apiClient.get(
    `/veranstaltungen/${veranstaltungId}/teilnehmer/available/paged`,
    {
      params: {
        page,
        size,
        search,
        verein,
      },
    },
  );

  return res.data;
}

/* ================= ASSIGNED ================= */

type TeilnehmerBackend = Omit<TeilnehmerList, "rolle"> & {
  rolle: string | null;
};

export async function getTeilnehmer(
  veranstaltungId: number,
  page: number,
  size: number,
  search?: string,
  verein?: string,
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
    },
  });

  // ✅ Mapping hier!
const content: TeilnehmerList[] = res.data.content.map((t) => ({
  ...t,
  rolle: mapRoleFromBackend(t.rolle),
}));

  return {
    ...res.data,
    content,
  };
}


/* ================= ADD BULK ================= */

export function addTeilnehmerBulk(veranstaltungId: number, personIds: number[]) {
  return apiClient.post(`/veranstaltungen/${veranstaltungId}/teilnehmer/bulk`, {
    personIds,
  });
}

/* ================= REMOVE BULK ================= */

export function removeTeilnehmerBulk(veranstaltungId: number, personIds: number[]) {
  return apiClient.delete(`/veranstaltungen/${veranstaltungId}/teilnehmer/bulk`, {
    data: { personIds },
  });
}

// teilnehmerApi.ts
export async function updateTeilnehmerRolle(
  veranstaltungId: number,
  personId: number,
  rolle: "L" | "M" | null,
) {

  console.log("API CALL", veranstaltungId, personId, rolle);
  return apiClient.put(`/veranstaltungen/${veranstaltungId}/teilnehmer/${personId}/rolle`, {
    rolle: mapRoleToBackend(rolle),
  });
}

export async function searchTeilnehmer(veranstaltungId: number, search: string) {
  const res = await apiClient.get(
    `/veranstaltungen/${veranstaltungId}/teilnehmer/search`, // ✅ RICHTIG
    {
      params: { search },
    },
  );

  return res.data ?? [];
}

export async function removeTeilnehmerFromGruppe(
  veranstaltungId: number,
  gruppeId: number,
  personId: number,
) {
  await apiClient.delete(
    `/veranstaltungen/${veranstaltungId}/finanzgruppen/${gruppeId}/teilnehmer/${personId}`,
  );
}
