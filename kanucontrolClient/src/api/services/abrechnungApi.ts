// src/api/services/abrechnungApi.ts

import apiClient from "@/api/client/apiClient";
import { AbrechnungDetail, Buchung, BelegCreate, BuchungCreate, BelegUpdate } from "@/api/types/abrechnung";

export async function getAbrechnung(
  veranstaltungId: number,
): Promise<AbrechnungDetail> {
  const { data } = await apiClient.get<AbrechnungDetail>(
    `/veranstaltungen/${veranstaltungId}/abrechnung`,
  );

  return data;
}

export async function synchronisiereAbrechnung(
  veranstaltungId: number,
): Promise<void> {
  await apiClient.post(
    `/veranstaltungen/${veranstaltungId}/abrechnung/synchronisieren`,
  );
}

export const addBuchung = async (
  veranstaltungId: number,
  belegId: number,
  payload: BuchungCreate,
) => {
  const { data } = await apiClient.post<Buchung>(
    `/veranstaltungen/${veranstaltungId}/abrechnung/belege/${belegId}/positionen`,
    payload,
  );
  return data;
};

export const updateBuchung = async (
  veranstaltungId: number,
  belegId: number,
  buchungId: number,
  payload: BuchungCreate,
) => {
  const { data } = await apiClient.put<Buchung>(
    `/veranstaltungen/${veranstaltungId}/abrechnung/belege/positionen/${buchungId}`,
    payload,
  );
  return data;
};

export const deleteBuchung = async (
  veranstaltungId: number,
  belegId: number,
  buchungId: number,
) => {
  await apiClient.delete(
    `/veranstaltungen/${veranstaltungId}/abrechnung/belege/positionen/${buchungId}`,
  );
};

export const abschliessenAbrechnung = async (
  veranstaltungId: number,
) => {
  await apiClient.post(
    `/veranstaltungen/${veranstaltungId}/abrechnung/abschliessen`,
  );
};

export const wiederOeffnenAbrechnung = async (veranstaltungId: number) => {
  await apiClient.post(`/veranstaltungen/${veranstaltungId}/abrechnung/wieder-oeffnen`);
};

export async function createBelegWithBuchung(
  veranstaltungId: number,
  payload: {
    beleg: BelegCreate;
    buchung: BuchungCreate;
  },
) {
  return apiClient.post(`/veranstaltungen/${veranstaltungId}/abrechnung/belege/mit-buchung`, payload);
}

export async function addBeleg(veranstaltungId: number, payload: BelegCreate): Promise<BelegCreate> {
  const res = await apiClient.post<BelegCreate>(
    `/veranstaltungen/${veranstaltungId}/abrechnung/belege`,
    payload,
  );

  return res.data;
}

export const deleteBeleg = async (veranstaltungId: number, belegId: number) => {
  await apiClient.delete(`/veranstaltungen/${veranstaltungId}/abrechnung/belege/${belegId}`);
};

export async function updateBeleg(veranstaltungId: number, belegId: number, data: BelegUpdate) {
  const response = await apiClient.put(
    `/veranstaltungen/${veranstaltungId}/abrechnung/belege/${belegId}`,
    data,
  );

  return response.data;
}