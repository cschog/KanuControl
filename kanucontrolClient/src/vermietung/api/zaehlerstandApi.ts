import apiClient from "@/core/api/client/apiClient";

export interface Zaehlerstand {
  id: number;
  zaehlerId: number;
  ablesedatum: string;
  zaehlerstand: number;
  bemerkung: string | null;
}

export interface ZaehlerstandSave {
  ablesedatum: string;
  zaehlerstand: number;
  bemerkung: string | null;
}

function baseUrl(zaehlerId: number): string {
  return `/vermietung/zaehler/${zaehlerId}/staende`;
}

/**
 * Alle Zählerstände eines Zählers laden.
 */
export async function getZaehlerstaende(zaehlerId: number): Promise<Zaehlerstand[]> {
  const response = await apiClient.get<Zaehlerstand[]>(baseUrl(zaehlerId));

  return response.data;
}

/**
 * Neuen Zählerstand erfassen.
 */
export async function createZaehlerstand(
  zaehlerId: number,
  payload: ZaehlerstandSave,
): Promise<Zaehlerstand> {
  const response = await apiClient.post<Zaehlerstand>(baseUrl(zaehlerId), payload);

  return response.data;
}

/**
 * Bestehenden Zählerstand bearbeiten.
 */
export async function updateZaehlerstand(
  zaehlerId: number,
  id: number,
  payload: ZaehlerstandSave,
): Promise<Zaehlerstand> {
  const response = await apiClient.put<Zaehlerstand>(`${baseUrl(zaehlerId)}/${id}`, payload);

  return response.data;
}

/**
 * Zählerstand löschen.
 */
export async function deleteZaehlerstand(zaehlerId: number, id: number): Promise<void> {
  await apiClient.delete(`${baseUrl(zaehlerId)}/${id}`);
}
