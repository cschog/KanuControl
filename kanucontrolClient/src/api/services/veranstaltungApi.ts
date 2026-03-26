import apiClient from "@/api/client/apiClient";
import { VeranstaltungList } from "@/api/types/VeranstaltungList";
import { VeranstaltungDetail } from "@/api/types/VeranstaltungDetail";
import { VeranstaltungSave } from "@/api/types/VeranstaltungSave";
import { Page } from "@/api/types/Page";

/* =========================================================
   LIST (PAGED)
   ========================================================= */

export function getVeranstaltungenPage(page: number, size: number) {
  return apiClient
    .get<Page<VeranstaltungList>>("/veranstaltungen", {
      params: {
        page,
        size,
        sort: "beginnDatum,desc",
      },
    })
    .then((r) => r.data.content);
}

/* ================= PDF Teilnehmerliste ================= */

export async function downloadTeilnehmerPdf(veranstaltungId: number) {
  const response = await apiClient.get(`/veranstaltungen/${veranstaltungId}/teilnehmer/pdf`, {
    responseType: "blob",
  });

  return response;
}

/* ================= PDF Erhebungsbogen ================= */

export async function downloadErhebungsbogenPdf(veranstaltungId: number) {
  const response = await apiClient.get(`/veranstaltungen/${veranstaltungId}/erhebungsbogen/pdf`, {
    responseType: "blob",
  });

  return response;
}

/* ================= PDF Anmeldung fm-jem ================= */

export async function downloadFmJemReport(id: number): Promise<Blob> {
  const response = await apiClient.get(`/veranstaltungen/${id}/fm-jem-report`, {
    responseType: "blob",
  });

  return response.data;
}


/* =========================================================
   DETAIL
   ========================================================= */

export function getVeranstaltung(id: number) {
  return apiClient.get<VeranstaltungDetail>(`/veranstaltungen/${id}`).then((r) => r.data);
}

/* =========================================================
   ACTIVE
   ========================================================= */

export async function getActiveVeranstaltung(): Promise<VeranstaltungDetail | null> {
  try {
    const { data } = await apiClient.get<VeranstaltungDetail>("/veranstaltungen/aktiv");
    return data;
  } catch (e: unknown) {
    if (
      typeof e === "object" &&
      e !== null &&
      "response" in e &&
      typeof (e as { response?: { status?: number } }).response?.status === "number" &&
      (e as { response?: { status?: number } }).response?.status === 404
    ) {
      return null; // keine aktive Veranstaltung
    }

    throw e;
  }
}

export function setActiveVeranstaltung(id: number) {
  return apiClient.put<VeranstaltungDetail>(`/veranstaltungen/${id}/aktiv`).then((r) => r.data);
}

/* =========================================================
   CREATE
   ========================================================= */

export function createVeranstaltung(payload: VeranstaltungSave) {
  return apiClient.post<VeranstaltungDetail>("/veranstaltungen", payload).then((r) => r.data);
}

/* =========================================================
   UPDATE
   ========================================================= */

export function updateVeranstaltung(id: number, payload: VeranstaltungSave) {
  return apiClient.put<VeranstaltungDetail>(`/veranstaltungen/${id}`, payload).then((r) => r.data);
}

/* =========================================================
   DELETE
   ========================================================= */

export function deleteVeranstaltung(id: number) {
  return apiClient.delete(`/veranstaltungen/${id}`);
}

export async function downloadTeilnehmerlistePdf(id: number) {
  return apiClient.get(`/veranstaltungen/${id}/teilnehmer/pdf`, {
    responseType: "blob",
  });
}
export async function downloadAbrechnungPdf(id: number) {
  const res = await apiClient.get(`/veranstaltungen/${id}/abrechnung/pdf`, {
    responseType: "blob",
  });
  return res.data;
}
