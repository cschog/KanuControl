import apiClient from "@/api/client/apiClient";
import { VeranstaltungList } from "@/api/types/veranstaltung/VeranstaltungList";
import { VeranstaltungDetail } from "@/api/types/veranstaltung/VeranstaltungDetail";
import { VeranstaltungSave } from "@/api/types/veranstaltung/VeranstaltungSave";
import { Page } from "@/api/types/Page";
import { SaveResponse } from "@/api/types/SaveResponse";

/* =========================================================
   LIST
   ========================================================= */

export function getVeranstaltungenPage(
  sortField = "beginnDatum",
  sortDirection: "asc" | "desc" = "desc",
) {
  return apiClient
    .get<Page<VeranstaltungList>>(
      "/veranstaltungen",
      {
        params: {
          page: 0,
          size: 1000,

          sort: `${sortField},${sortDirection}`,
        },
      },
    )
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

export async function downloadFmJemReport(id: number) {
  return apiClient.get(`/veranstaltungen/${id}/fm-jem-report`, {
    responseType: "blob",
  });
}

/* ================= PDF Abrechnung ================= */

export async function downloadAbrechnungPdf(id: number) {
  return apiClient.get(`/veranstaltungen/${id}/abrechnung/pdf`, {
    responseType: "blob",
  });
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
  return apiClient
    .post<SaveResponse<VeranstaltungDetail>>("/veranstaltungen", payload)
    .then((r) => r.data);
}

/* =========================================================
   UPDATE
   ========================================================= */

export function updateVeranstaltung(id: number, payload: VeranstaltungSave) {
  return apiClient
    .put<SaveResponse<VeranstaltungDetail>>(`/veranstaltungen/${id}`, payload)
    .then((r) => r.data);
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

