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
  teilnehmerIds: number[],
): Promise<void> {
  await apiClient.put(
    `/veranstaltungen/${veranstaltungId}/finanzgruppen/${gruppeId}/teilnehmer`,
    teilnehmerIds,
  );
}

export async function deleteFinanzGruppe(veranstaltungId: number, gruppeId: number): Promise<void> {
  await apiClient.delete(`/veranstaltungen/${veranstaltungId}/finanzgruppen/${gruppeId}`);
}
