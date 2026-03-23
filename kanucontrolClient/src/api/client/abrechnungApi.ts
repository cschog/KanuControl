import apiClient from "@/api/client/apiClient";
import { AbrechnungDetail, Buchung, AbrechnungBeleg, BuchungCreate } from "@/api/types/abrechnung";

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
    `/veranstaltungen/${veranstaltungId}/abrechnung/belege/${belegId}/buchungen`,
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
    `/veranstaltungen/${veranstaltungId}/abrechnung/belege/${belegId}/buchungen/${buchungId}`,
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
    `/veranstaltungen/${veranstaltungId}/abrechnung/belege/${belegId}/buchungen/${buchungId}`,
  );
};

export const einreichenAbrechnung = async (veranstaltungId: number) => {
  await apiClient.post(`/veranstaltungen/${veranstaltungId}/abrechnung/einreichen`);
};

export const wiederOeffnenAbrechnung = async (veranstaltungId: number) => {
  await apiClient.post(`/veranstaltungen/${veranstaltungId}/abrechnung/wieder-oeffnen`);
};

export interface BelegCreate {
  kuerzel: string;
  datum: string;
  beschreibung?: string;
}

export async function addBeleg(
  veranstaltungId: number,
  payload: BelegCreate,
): Promise<AbrechnungBeleg> {
  const res = await apiClient.post<AbrechnungBeleg>(
    `/veranstaltungen/${veranstaltungId}/abrechnung/belege`,
    payload,
  );

  return res.data;
}