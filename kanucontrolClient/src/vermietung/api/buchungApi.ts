import type { AblesungBestaetigung } from "@/vermietung/types/VerbrauchskostenVorschau";
import apiClient from "@/core/api/client/apiClient";
import { isAxiosError } from "axios";

import type { Buchung } from "@/vermietung/types/Buchung";
import type { BuchungSave } from "@/vermietung/types/BuchungSave";
import type { Buchungsquelle } from "@/vermietung/enums/Buchungsquelle";
import type { Buchungsstatus } from "@/vermietung/enums/Buchungsstatus";

function baseUrl() {
  return "/vermietung/buchungen";
}

export async function getBuchungen(): Promise<Buchung[]> {
  const response = await apiClient.get<Buchung[]>(baseUrl());

  return response.data;
}

export async function getBuchung(id: number): Promise<Buchung> {
  const response = await apiClient.get<Buchung>(`${baseUrl()}/${id}`);

  return response.data;
}

export async function getPreis(
  mietbereichId: number,
  buchungsquelle: Buchungsquelle,
  stichtag: string,
): Promise<number> {
  const response = await apiClient.get<number>(`${baseUrl()}/preis`, {
    params: {
      mietbereichId,
      buchungsquelle,
      stichtag,
    },
  });

  return response.data;
}

export async function createBuchung(buchung: BuchungSave): Promise<Buchung> {
  const response = await apiClient.post<Buchung>(baseUrl(), buchung);

  return response.data;
}

export async function updateBuchung(id: number, buchung: BuchungSave): Promise<Buchung> {
  const response = await apiClient.put<Buchung>(`${baseUrl()}/${id}`, buchung);

  return response.data;
}

export function brauchtAbrechnungsLoeschbestaetigung(error: unknown): boolean {
  return isAxiosError(error) && error.response?.status === 409
    && error.response.data?.error === "BUCHUNG_ABRECHNUNGEN_BESTAETIGEN";
}

export async function deleteBuchung(id: number, archivierteAbrechnungenBestaetigt = false): Promise<void> {
  await apiClient.delete(`${baseUrl()}/${id}`, {
    params: { archivierteAbrechnungenBestaetigt },
  });
}

export async function aendereBuchungsstatus(id: number, status: Buchungsstatus, ablesungenBestaetigt: AblesungBestaetigung[] = [], sendEmail = false, fingerprint?: string): Promise<Buchung> {
  const response = await apiClient.patch<Buchung>(`${baseUrl()}/${id}/status`, { status, ablesungenBestaetigt, sendEmail, fingerprint });

  return response.data;
}

export interface BuchungsnummerAenderung { id: number; buchungsnummer: string }
export async function aendereBuchungsnummer(id: number, buchungsnummer: string, bisherigeBuchungsnummer: string): Promise<BuchungsnummerAenderung> {
  return (await apiClient.patch<BuchungsnummerAenderung>(`${baseUrl()}/${id}/buchungsnummer`, { buchungsnummer, bisherigeBuchungsnummer })).data;
}
