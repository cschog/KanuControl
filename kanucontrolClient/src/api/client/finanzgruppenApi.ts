import  apiClient  from "@/api/client/apiClient";

export async function getFinanzgruppen(veranstaltungId: number) {
  const res = await apiClient.get(`/veranstaltungen/${veranstaltungId}/finanzgruppen`);
  return res.data;
}

export async function createFinanzgruppe(veranstaltungId: number, kuerzel: string) {
  const res = await apiClient.post(`/veranstaltungen/${veranstaltungId}/finanzgruppen`, {
    kuerzel,
  });
  return res.data;
}

export async function assignTeilnehmerBulk(
  veranstaltungId: number,
  gruppeId: number,
  teilnehmerIds: number[],
) {
  await apiClient.put(
    `/veranstaltungen/${veranstaltungId}/finanzgruppen/${gruppeId}/teilnehmer`,
    teilnehmerIds,
  );
}
