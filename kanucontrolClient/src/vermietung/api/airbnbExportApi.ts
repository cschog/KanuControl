import apiClient from "@/core/api/client/apiClient";
export interface AirbnbExport {
  aktiv: boolean;
  mietbereichIds: number[];
  version: number;
  linkVorhanden: boolean;
  feedPfad: string | null;
}
const url = (id: number) => `/vermietung/objekte/${id}/airbnb-export`;
export async function getAirbnbExport(id: number): Promise<AirbnbExport> {
  return (await apiClient.get<AirbnbExport>(url(id))).data;
}
export async function saveAirbnbExport(id: number, input: AirbnbExport, neuerLink = false): Promise<AirbnbExport> {
  const payload = { aktiv: input.aktiv, mietbereichIds: input.mietbereichIds, version: input.version };
  return neuerLink ? (await apiClient.post<AirbnbExport>(`${url(id)}/link`, payload)).data
    : (await apiClient.put<AirbnbExport>(url(id), payload)).data;
}
