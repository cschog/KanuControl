import apiClient from "@/api/client/apiClient";

/* ================= TYPES ================= */

export interface TeilnehmerKurz {
  id: number;
  personId: number;
  vorname: string;
  nachname: string;
}

export interface FinanzGruppe {
  id: number;
  kuerzel: string;
  belegCount: number;
  einnahmen: number;
  ausgaben: number;
  saldo: number;
  system: boolean;
  teilnehmer: TeilnehmerKurz[];
}

/* ================= API ================= */

export async function getFinanzgruppen(veranstaltungId: number): Promise<FinanzGruppe[]> {
  const res = await apiClient.get<FinanzGruppe[]>(
    `/veranstaltungen/${veranstaltungId}/finanzgruppen`,
  );
  return res.data;
}

export async function createFinanzgruppe(
  veranstaltungId: number,
  kuerzel: string,
): Promise<FinanzGruppe> {
  const res = await apiClient.post<FinanzGruppe>(
    `/veranstaltungen/${veranstaltungId}/finanzgruppen`,
    { kuerzel },
  );
  return res.data;
}

export async function assignTeilnehmerBulk(
  veranstaltungId: number,
  gruppeId: number,
  personIds: number[],
): Promise<void> {
  await apiClient.put(
    `/veranstaltungen/${veranstaltungId}/finanzgruppen/${gruppeId}/teilnehmer`,
    personIds,
  );
}

export async function removeTeilnehmerFromFinanzgruppe(
  veranstaltungId: number,
  gruppeId: number,
  personId: number,
): Promise<void> {
  await apiClient.delete(
    `/veranstaltungen/${veranstaltungId}/finanzgruppen/${gruppeId}/teilnehmer/${personId}`,
  );
}

export async function deleteFinanzGruppe(veranstaltungId: number, gruppeId: number): Promise<void> {
  await apiClient.delete(`/veranstaltungen/${veranstaltungId}/finanzgruppen/${gruppeId}`);
}
