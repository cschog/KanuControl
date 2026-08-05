import apiClient from "@/api/client/apiClient";

import { BelegDokumentDTO } from "@/api/types/abrechnung";

export async function findAll(belegId: number): Promise<BelegDokumentDTO[]> {
  const response = await apiClient.get<BelegDokumentDTO[]>(`/belege/${belegId}/dokumente`);

  return response.data;
}

export async function upload(belegId: number, file: File): Promise<BelegDokumentDTO> {
  const formData = new FormData();
  formData.append("file", file);

  const response = await apiClient.post<BelegDokumentDTO>(`/belege/${belegId}/dokumente`, formData);

  return response.data;
}

export async function download(dokumentId: number): Promise<Blob> {
  const response = await apiClient.get<Blob>(`/belege/dokumente/${dokumentId}`, {
    responseType: "blob",
  });

  return response.data;
}

export async function deleteBelegDokument(dokumentId: number): Promise<void> {
  await apiClient.delete(`/belege/dokumente/${dokumentId}`);
}
