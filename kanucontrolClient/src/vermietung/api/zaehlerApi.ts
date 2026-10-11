import apiClient from "@/core/api/client/apiClient";

export type Verbrauchsart = "STROM" | "GAS" | "WASSER";

export interface Zaehler {
  id: number;
  mietobjektId: number;
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

function baseUrl(mietobjektId: number) {
  return `/vermietung/objekte/${mietobjektId}/zaehler`;
}

export async function getZaehler(mietobjektId: number): Promise<Zaehler[]> {
  const response = await apiClient.get<Zaehler[]>(baseUrl(mietobjektId));

  return response.data;
}

export async function getZaehlerById(mietobjektId: number, id: number): Promise<Zaehler> {
  const response = await apiClient.get<Zaehler>(`${baseUrl(mietobjektId)}/${id}`);

  return response.data;
}

export async function createZaehler(mietobjektId: number, zaehler: ZaehlerSave): Promise<Zaehler> {
  const response = await apiClient.post<Zaehler>(baseUrl(mietobjektId), zaehler);

  return response.data;
}

export async function updateZaehler(
  mietobjektId: number,
  id: number,
  zaehler: ZaehlerSave,
): Promise<Zaehler> {
  const response = await apiClient.put<Zaehler>(`${baseUrl(mietobjektId)}/${id}`, zaehler);

  return response.data;
}

export async function deleteZaehler(mietobjektId: number, id: number): Promise<void> {
  await apiClient.delete(`${baseUrl(mietobjektId)}/${id}`);
}
