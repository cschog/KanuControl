// api/services/teilnehmerApi.ts

import apiClient from "@/api/client/apiClient";

/* ================= AVAILABLE ================= */

export async function getAvailablePersons(
  veranstaltungId: number,
  page: number,
  size: number,
  name?: string,
  vorname?: string,
  verein?: string,
) {
  const res = await apiClient.get(`/veranstaltungen/${veranstaltungId}/teilnehmer/available/paged`, {
    params: {
      page,
      size,
      name: name || undefined,
      vorname: vorname || undefined,
      verein: verein || undefined,
    },
  });

  return res.data;
}

/* ================= ASSIGNED ================= */

export async function getTeilnehmer(
  veranstaltungId: number,
  page: number,
  size: number,
  name?: string,
  vorname?: string,
  verein?: string,
) {
  const res = await apiClient.get(`/veranstaltungen/${veranstaltungId}/teilnehmer/paged`, {
    params: {
      page,
      size,
      name: name || undefined,
      vorname: vorname || undefined,
      verein: verein || undefined,
    },
  });

  return res.data;
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
  return apiClient.put(
    `/veranstaltungen/${veranstaltungId}/teilnehmer/${personId}/rolle`,
    { rolle }, // DTO für Backend bleibt hier
  );
}

export async function searchTeilnehmer(veranstaltungId: number, search: string) {
  const res = await apiClient.get(`/veranstaltungen/${veranstaltungId}/teilnehmer`, {
    params: {
      search,
      size: 300,
      page: 0,
    },
  });

  return res.data.content;
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
