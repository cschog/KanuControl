// src/api/services/zahlungsnachweisApi.ts

import apiClient from "@/api/client/apiClient";
import {
  FinanzGruppeZahlungDTO,
  UeberzahlungTeilnehmerkontoPruefungDTO,
} from "@/api/types/beitraege";
import { DokumentDTO } from "@/api/types/dokument";
import { ReferenzObjekt } from "@/api/enums/ReferenzObjekt";

import { OffeneUeberzahlungDTO } from "@/api/types/beitraege";
import { RueckzahlungTeilnehmerbeitragCreate } from "@/api/types/abrechnung";

export async function findAll(
  veranstaltungId: number,
  zahlungsnachweisId: number,
): Promise<DokumentDTO[]> {
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
  referenzObjekt?: ReferenzObjekt | null,
): Promise<DokumentDTO> {
  const formData = new FormData();

  formData.append("file", file);

  if (referenzObjekt) {
    formData.append("referenzObjekt", referenzObjekt);
  }

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

export async function getZahlungenByFinanzGruppe(
  veranstaltungId: number,
  finanzGruppeId: number,
): Promise<FinanzGruppeZahlungDTO[]> {
  const response = await apiClient.get<FinanzGruppeZahlungDTO[]>(
    `/veranstaltungen/${veranstaltungId}/zahlungsnachweise/finanzgruppe/${finanzGruppeId}`,
  );

  return response.data;
}

export async function updateReferenzObjekt(
  veranstaltungId: number,
  zahlungsnachweisId: number,
  dokumentId: number,
  referenzObjekt: ReferenzObjekt,
): Promise<DokumentDTO> {
  const response = await apiClient.put<DokumentDTO>(
    `/veranstaltungen/${veranstaltungId}/zahlungsnachweise/${zahlungsnachweisId}/dokumente/${dokumentId}/referenz-objekt`,
    null,
    {
      params: {
        referenzObjekt,
      },
    },
  );

  return response.data;
}

export async function pruefeUeberzahlungTeilnehmerkonto(
  veranstaltungId: number,
  zahlungsnachweisId: number,
): Promise<UeberzahlungTeilnehmerkontoPruefungDTO> {
  const { data } = await apiClient.get<UeberzahlungTeilnehmerkontoPruefungDTO>(
    `/veranstaltungen/${veranstaltungId}/zahlungsnachweise/${zahlungsnachweisId}/ueberzahlung-teilnehmerkonto-pruefung`,
  );

  return data;
}


export async function getOffeneUeberzahlungen(
  veranstaltungId: number,
): Promise<OffeneUeberzahlungDTO[]> {
  const { data } = await apiClient.get<OffeneUeberzahlungDTO[]>(
    `/veranstaltungen/${veranstaltungId}/zahlungsnachweise/offene-ueberzahlungen`,
  );

  return data;
}

export async function rueckzahlungTeilnehmerbeitrag(
  veranstaltungId: number,
  payload: RueckzahlungTeilnehmerbeitragCreate,
): Promise<void> {
  await apiClient.post(
    `/veranstaltungen/${veranstaltungId}/zahlungsnachweise/rueckzahlung`,
    payload,
  );
}