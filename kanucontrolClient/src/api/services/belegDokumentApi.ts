import apiClient from "@/api/client/apiClient";
import { optimizeUploadFile } from "@/utils/imageUtils";
import { BelegDokumentDTO } from "@/api/types/abrechnung";

export async function findAll(belegId: number): Promise<BelegDokumentDTO[]> {
  const response = await apiClient.get<BelegDokumentDTO[]>(`/belege/${belegId}/dokumente`);

  return response.data;
}

export async function upload(belegId: number, file: File): Promise<BelegDokumentDTO> {
  let uploadFile = file;

  try {
    uploadFile = await optimizeUploadFile(file);

    if (uploadFile !== file) {
      console.info(
        `Bild komprimiert: ${file.name} (${(file.size / 1024 / 1024).toFixed(2)} MB → ${(uploadFile.size / 1024 / 1024).toFixed(2)} MB)`,
      );
    }
  } catch (error) {
    console.warn("Bild konnte nicht optimiert werden. Original wird hochgeladen.", error);
  }

  const formData = new FormData();
  formData.append("file", uploadFile);

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

export async function preview(dokumentId: number): Promise<void> {
  const blob = await download(dokumentId);

  const url = URL.createObjectURL(blob);

  window.open(url, "_blank", "noopener,noreferrer");

  setTimeout(() => URL.revokeObjectURL(url), 60_000);
}
