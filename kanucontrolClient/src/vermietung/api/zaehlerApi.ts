import apiClient from "@/core/api/client/apiClient";

export type Verbrauchsart = "STROM" | "GAS" | "WASSER";

export interface Zaehler {
  id: number;
  bezeichnung: string;
  verbrauchsart: Verbrauchsart;
  zaehlernummer: string | null;
  mengeneinheit: string;
  aktiv: boolean;
}

export interface ZaehlerSave {
  bezeichnung: string;
  verbrauchsart: Verbrauchsart;
  zaehlernummer: string | null;
  mengeneinheit: string;
  aktiv: boolean;
}

const baseUrl = "/vermietung/zaehler";

export async function getZaehler(): Promise<Zaehler[]> {
  const response = await apiClient.get<Zaehler[]>(baseUrl);

  return response.data;
}

export async function getZaehlerById(id: number): Promise<Zaehler> {
  const response = await apiClient.get<Zaehler>(`${baseUrl}/${id}`);

  return response.data;
}

export async function createZaehler(zaehler: ZaehlerSave): Promise<Zaehler> {
  const response = await apiClient.post<Zaehler>(baseUrl, zaehler);

  return response.data;
}

export async function updateZaehler(id: number, zaehler: ZaehlerSave): Promise<Zaehler> {
  const response = await apiClient.put<Zaehler>(`${baseUrl}/${id}`, zaehler);

  return response.data;
}

export async function deleteZaehler(id: number): Promise<void> {
  await apiClient.delete(`${baseUrl}/${id}`);
}
