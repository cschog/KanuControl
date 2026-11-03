import apiClient from "@/core/api/client/apiClient";
export interface Sperrzeit { id: number; version: number; von: string; bis: string; ganztaegig: boolean; grund: string; }
export type SperrzeitSave = Omit<Sperrzeit, "id" | "version"> & { version?: number };
const url = (objektId: number) => `/vermietung/objekte/${objektId}/sperrzeiten`;
export async function getSperrzeiten(objektId: number): Promise<Sperrzeit[]> {
  return (await apiClient.get<Sperrzeit[]>(url(objektId))).data;
}
export async function saveSperrzeit(objektId: number, id: number | null, input: SperrzeitSave): Promise<Sperrzeit> {
  return id === null ? (await apiClient.post<Sperrzeit>(url(objektId), input)).data
    : (await apiClient.put<Sperrzeit>(`${url(objektId)}/${id}`, input)).data;
}
export async function deleteSperrzeit(objektId: number, item: Sperrzeit): Promise<void> {
  await apiClient.delete(`${url(objektId)}/${item.id}`, { params: { version: item.version } });
}
