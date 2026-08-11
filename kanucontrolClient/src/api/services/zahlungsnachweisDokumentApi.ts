// src/api/services/zahlungsnachweisDokumentApi.ts

import apiClient from "@/api/client/apiClient";
import { ZahlungsnachweisDokumentDTO } from "@/api/types/beitraege";

export async function findAll(
  veranstaltungId: number,
  zahlungsnachweisId: number,
): Promise<ZahlungsnachweisDokumentDTO[]> {
  const response = await apiClient.get(
    `/veranstaltungen/${veranstaltungId}/zahlungsnachweise/${zahlungsnachweisId}/dokumente`,
  );

  const data = response.data?.data ?? response.data;

  return Array.isArray(data) ? data : [];
}

export async function upload(
  veranstaltungId: number,
  zahlungsnachweisId: number,
  file: File,
): Promise<ZahlungsnachweisDokumentDTO> {
  const formData = new FormData();

  formData.append("file", file);

  const response = await apiClient.post(
    `/veranstaltungen/${veranstaltungId}/zahlungsnachweise/${zahlungsnachweisId}/dokumente`,
    formData,
  );

  return response.data.data;
}

export async function deleteZahlungsnachweisDokument(
  veranstaltungId: number,
  zahlungsnachweisId: number,
  dokumentId: number,
): Promise<void> {
  await apiClient.delete(
    `/veranstaltungen/${veranstaltungId}/zahlungsnachweise/${zahlungsnachweisId}/dokumente/${dokumentId}`,
  );
}

export async function download(
  veranstaltungId: number,
  zahlungsnachweisId: number,
  dokumentId: number,
): Promise<Blob> {
  const response = await apiClient.get(
    `/veranstaltungen/${veranstaltungId}/zahlungsnachweise/${zahlungsnachweisId}/dokumente/${dokumentId}/download`,
    {
      responseType: "blob",
    },
  );

  return response.data;
}