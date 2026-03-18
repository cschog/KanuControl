import apiClient from "@/api/client/apiClient";
import { PlanungDetail, PlanungPosition, PlanungPositionCreate } from "@/api/types/planung";

export const getPlanung = async (veranstaltungId: number) => {
  const { data } = await apiClient.get<PlanungDetail>(
    `/veranstaltungen/${veranstaltungId}/planung`,
  );
  return data;
};

export const addPosition = async (veranstaltungId: number, payload: PlanungPositionCreate) => {
  const { data } = await apiClient.post<PlanungPosition>(
    `/veranstaltungen/${veranstaltungId}/planung/positionen`,
    payload,
  );
  return data;
};

export const updatePosition = async (
  veranstaltungId: number,
  positionId: number,
  payload: PlanungPositionCreate,
) => {
  const { data } = await apiClient.put<PlanungPosition>(
    `/veranstaltungen/${veranstaltungId}/planung/positionen/${positionId}`,
    payload,
  );
  return data;
};

export const deletePosition = async (veranstaltungId: number, positionId: number) => {
  await apiClient.delete(`/veranstaltungen/${veranstaltungId}/planung/positionen/${positionId}`);
};

export const einreichen = async (veranstaltungId: number) => {
  await apiClient.post(`/veranstaltungen/${veranstaltungId}/planung/einreichen`);
};

export const wiederOeffnen = async (veranstaltungId: number) => {
  await apiClient.post(`/veranstaltungen/${veranstaltungId}/planung/wieder-oeffnen`);
};