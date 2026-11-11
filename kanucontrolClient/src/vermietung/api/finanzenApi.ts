import apiClient from "@/core/api/client/apiClient";
import type { BuchungFinanzen, Finanzbewegungsart, Finanzkategorie } from "@/vermietung/types/Finanzen";
export async function getBuchungFinanzen(): Promise<BuchungFinanzen[]> {
  const { data } = await apiClient.get<BuchungFinanzen[]>("/vermietung/finanzen");
  return data;
}
export async function erfasseFinanzbewegung(buchungId: number, data: {
  kategorie: Finanzkategorie; art: Finanzbewegungsart; betrag: number; datum: string; bemerkung?: string;
}): Promise<BuchungFinanzen> {
  const response = await apiClient.post<BuchungFinanzen>(`/vermietung/finanzen/${buchungId}/bewegungen`, data);
  return response.data;
}

export async function getBuchungFinanzenById(id: number): Promise<BuchungFinanzen> {
  return (await apiClient.get<BuchungFinanzen>(`/vermietung/finanzen/${id}`)).data;
}
export async function verrechneAbschlussKaution(id: number, data: {
  betrag: number; datum: string; ablesungen: import("@/vermietung/types/VerbrauchskostenVorschau").AblesungBestaetigung[];
}): Promise<BuchungFinanzen> {
  return (await apiClient.post<BuchungFinanzen>(`/vermietung/finanzen/${id}/kautionsverrechnung`, data)).data;
}
