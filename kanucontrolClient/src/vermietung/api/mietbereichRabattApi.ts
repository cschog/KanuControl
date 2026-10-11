import apiClient  from "@/core/api/client/apiClient";

import type { MietbereichRabatt } from "@/vermietung/types/MietbereichRabatt";
import type { MietbereichRabattSave } from "@/vermietung/types/MietbereichRabattSave";

const basePath = (mietobjektId: number, mietbereichId: number) =>
  `/vermietung/objekte/${mietobjektId}/bereiche/${mietbereichId}/rabatte`;

export async function getMietbereichRabatte(
  mietobjektId: number,
  mietbereichId: number,
): Promise<MietbereichRabatt[]> {
  const response = await apiClient.get<MietbereichRabatt[]>(basePath(mietobjektId, mietbereichId));

  return response.data;
}

export async function createMietbereichRabatt(
  mietobjektId: number,
  mietbereichId: number,
  payload: MietbereichRabattSave,
): Promise<MietbereichRabatt> {
  const response = await apiClient.post<MietbereichRabatt>(
    basePath(mietobjektId, mietbereichId),
    payload,
  );

  return response.data;
}

export async function updateMietbereichRabatt(
  mietobjektId: number,
  mietbereichId: number,
  id: number,
  payload: MietbereichRabattSave,
): Promise<MietbereichRabatt> {
  const response = await apiClient.put<MietbereichRabatt>(
    `${basePath(mietobjektId, mietbereichId)}/${id}`,
    payload,
  );

  return response.data;
}

export async function deleteMietbereichRabatt(
  mietobjektId: number,
  mietbereichId: number,
  id: number,
): Promise<void> {
  await apiClient.delete(`${basePath(mietobjektId, mietbereichId)}/${id}`);
}
