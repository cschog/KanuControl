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
  const res = await apiClient.get(
    `/veranstaltung/${veranstaltungId}/teilnehmer/available`,
    {
      params: {
        page,
        size,
        name: name || undefined,
        vorname: vorname || undefined,
        verein: verein || undefined,
      },
    },
  );

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
  const res = await apiClient.get(
    `/veranstaltung/${veranstaltungId}/teilnehmer`,
    {
      params: {
        page,
        size,
        name: name || undefined,
        vorname: vorname || undefined,
        verein: verein || undefined,
      },
    },
  );

  return res.data;
}

/* ================= ADD BULK ================= */

export function addTeilnehmerBulk(veranstaltungId: number, personIds: number[]) {
  return apiClient.post(`/veranstaltung/${veranstaltungId}/teilnehmer/bulk`, {
    personIds,
  });
}

/* ================= REMOVE BULK ================= */

export function removeTeilnehmerBulk(veranstaltungId: number, personIds: number[]) {
  return apiClient.delete(`/veranstaltung/${veranstaltungId}/teilnehmer/bulk`, {
    data: { personIds },
  });
}
