import apiClient from "@/core/api/client/apiClient";
import { DokumentDTO } from "@/kjfp/types/dokument";
import { ReferenzObjekt } from "@/kjfp/api/enums/ReferenzObjekt";

export async function findAll(belegId: number): Promise<DokumentDTO[]> {
  const response = await apiClient.get<DokumentDTO[]>(`/belege/${belegId}/dokumente`);

  return response.data;
}

export async function upload(
  belegId: number,
  file: File,
  referenzObjekt?: ReferenzObjekt | null,
): Promise<DokumentDTO> {
  // Original unverändert aufbewahren; Orientierung wird separat gespeichert.
  const formData = new FormData();

  formData.append("file", file);

  if (referenzObjekt) {
    formData.append("referenzObjekt", referenzObjekt);
  }

  const response = await apiClient.post<DokumentDTO>(`/belege/${belegId}/dokumente`, formData);

  return response.data;
}

export async function download(belegId: number, dokumentId: number): Promise<Blob> {
  const response = await apiClient.get<Blob>(`/belege/${belegId}/dokumente/${dokumentId}`, {
    responseType: "blob",
  });

  return response.data;
}

export async function deleteBelegDokument(belegId: number, dokumentId: number): Promise<void> {
  await apiClient.delete(`/belege/${belegId}/dokumente/${dokumentId}`);
}

export async function preview(belegId: number, dokumentId: number): Promise<void> {
  const previewWindow = window.open("", "_blank");

  if (!previewWindow) {
    return;
  }

  try {
    const blob = await download(belegId, dokumentId);

    const url = URL.createObjectURL(blob);

    previewWindow.location.href = url;

    setTimeout(() => URL.revokeObjectURL(url), 60_000);
  } catch (error) {
    previewWindow.close();
    throw error;
  }
}

export async function updateReferenzObjekt(
  belegId: number,
  dokumentId: number,
  referenzObjekt: ReferenzObjekt,
): Promise<DokumentDTO> {
  const response = await apiClient.put<DokumentDTO>(
    `/belege/${belegId}/dokumente/${dokumentId}/referenz-objekt`,
    null,
    {
      params: {
        referenzObjekt,
      },
    },
  );

  return response.data;
}

export interface DokumentAusrichtung { drehungen: number[]; version: number }
export async function getAusrichtung(belegId: number, dokumentId: number): Promise<DokumentAusrichtung> {
  return (await apiClient.get<DokumentAusrichtung>(`/belege/${belegId}/dokumente/${dokumentId}/ausrichtung`)).data;
}
export async function saveAusrichtung(belegId: number, dokumentId: number, value: DokumentAusrichtung): Promise<DokumentAusrichtung> {
  return (await apiClient.put<DokumentAusrichtung>(`/belege/${belegId}/dokumente/${dokumentId}/ausrichtung`, value)).data;
}
export async function getVorschauSeite(belegId: number, dokumentId: number, seite: number): Promise<Blob> {
  return (await apiClient.get<Blob>(`/belege/${belegId}/dokumente/${dokumentId}/vorschau/${seite}`, { responseType: "blob" })).data;
}
export function downloadDatei(blob: Blob, name: string) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a"); link.href = url;
  link.download = blob.type === "image/png" ? name.replace(/\.[^.]+$/, "") + ".png" : name;
  link.click(); setTimeout(() => URL.revokeObjectURL(url), 1000);
}
