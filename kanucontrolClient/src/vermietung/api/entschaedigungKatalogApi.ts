import apiClient from "@/core/api/client/apiClient";
import type { EntschaedigungKatalog, EntschaedigungKatalogSave } from "@/vermietung/types/EntschaedigungKatalog";
const url = (objektId: number) => `/vermietung/objekte/${objektId}/entschaedigungen`;
export async function getEntschaedigungKatalog(objektId: number): Promise<EntschaedigungKatalog[]> {
  return (await apiClient.get<EntschaedigungKatalog[]>(url(objektId))).data;
}
export async function saveEntschaedigungKatalog(objektId: number, id: number | null, data: EntschaedigungKatalogSave): Promise<EntschaedigungKatalog> {
  return (await (id == null
    ? apiClient.post<EntschaedigungKatalog>(url(objektId), data)
    : apiClient.put<EntschaedigungKatalog>(`${url(objektId)}/${id}`, data))).data;
}
