import apiClient from "@/api/client/apiClient";
import { AbrechnungDetail, Buchung, BelegCreate, BuchungCreate, BelegUpdate } from "@/api/types/abrechnung";
import { ValidationResult } from "@/api/types/ValidationResult";

export const getAbrechnung = async (veranstaltungId: number) => {
  const { data } = await apiClient.get<AbrechnungDetail>(
    `/veranstaltungen/${veranstaltungId}/abrechnung`,
  );
  return data;
};

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

export const einreichenAbrechnung = async (veranstaltungId: number) => {
  await apiClient.post(`/veranstaltungen/${veranstaltungId}/abrechnung/einreichen`);
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

export async function validateAbrechnung(veranstaltungId: number): Promise<ValidationResult> {
  const res = await apiClient.get<ValidationResult>(
    `/veranstaltungen/${veranstaltungId}/abrechnung/validierung`,
  );

  return res.data;
}