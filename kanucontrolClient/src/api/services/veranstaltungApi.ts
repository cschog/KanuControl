import apiClient from "@/api/client/apiClient";
import { Page } from "@/api/types/Page";
import { VeranstaltungList } from "@/api/types/VeranstaltungList";
import { VeranstaltungDetail } from "@/api/types/VeranstaltungDetail";
import { VeranstaltungSave } from "@/api/types/VeranstaltungSave";

/* =========================================================
   LIST (PAGED)
   ========================================================= */

export function getVeranstaltungenPage(page: number, size: number) {
  return apiClient
    .get<Page<VeranstaltungList>>("/veranstaltung", {
      params: {
        page,
        size,
        sort: "beginnDatum,desc",
      },
    })
    .then((r) => r.data);
}

/* ================= PDF Teilnehmerliste ================= */

export async function downloadTeilnehmerPdf(veranstaltungId: number) {
  const response = await apiClient.get(`/veranstaltung/${veranstaltungId}/teilnehmer/pdf`, {
    responseType: "blob", // ⭐ wichtig für PDF
  });

  return response;
}

/* =========================================================
   DETAIL
   ========================================================= */

export function getVeranstaltung(id: number) {
  return apiClient.get<VeranstaltungDetail>(`/veranstaltung/${id}`).then((r) => r.data);
}

/* =========================================================
   ACTIVE
   ========================================================= */

export async function getActiveVeranstaltung(): Promise<VeranstaltungDetail | null> {
  try {
    const { data } = await apiClient.get<VeranstaltungDetail>("/veranstaltung/active");
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
  return apiClient.post<VeranstaltungDetail>(`/veranstaltung/${id}/activate`).then((r) => r.data);
}



/* =========================================================
   CREATE
   ========================================================= */

export function createVeranstaltung(payload: VeranstaltungSave) {
  return apiClient.post<VeranstaltungDetail>("/veranstaltung", payload).then((r) => r.data);
}

/* =========================================================
   UPDATE
   ========================================================= */

export function updateVeranstaltung(id: number, payload: VeranstaltungSave) {
  return apiClient.put<VeranstaltungDetail>(`/veranstaltung/${id}`, payload).then((r) => r.data);
}

/* =========================================================
   DELETE
   ========================================================= */

export function deleteVeranstaltung(id: number) {
  return apiClient.delete(`/veranstaltung/${id}`);
}
