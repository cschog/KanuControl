import apiClient from "@/core/api/client/apiClient";
import type Verein from "@/core/api/types/verein/VereinFormModel";
import type { VereinSave } from "@/core/api/types/verein/VereinSave";

export type Veranstalter = Verein & { id: number; buchungen: number };
export type VeranstalterSave = Omit<VereinSave, "telefon" | "schutzkonzept" | "kikZertifiziertSeit" | "kikZertifiziertBis">;
const BASE = "/vermietung/veranstalter";
export async function searchVeranstalter(params: { search?: string } = {}): Promise<Veranstalter[]> {
  return (await apiClient.get<Veranstalter[]>(BASE, { params })).data;
}
export async function saveVeranstalter(id: number | undefined, payload: VeranstalterSave): Promise<Verein> {
  return id === undefined
    ? (await apiClient.post<Verein>(BASE, payload)).data
    : (await apiClient.put<Verein>(`${BASE}/${id}`, payload)).data;
}
