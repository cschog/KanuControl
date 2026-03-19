import  apiClient  from "@/api/client/apiClient";

export async function searchTeilnehmer(veranstaltungId: number, search: string) {
  const res = await apiClient.get(
    `/veranstaltungen/${veranstaltungId}/teilnehmer`, 
    {
      params: {
        search,
        size: 300,
        page: 0,
      },
    },
  );

  return res.data.content;
}

export async function removeTeilnehmerFromGruppe(
  veranstaltungId: number,
  gruppeId: number,
  personId: number,
) {
  await apiClient.delete(
    `/veranstaltungen/${veranstaltungId}/finanzgruppen/${gruppeId}/teilnehmer/${personId}`,
  );
}
