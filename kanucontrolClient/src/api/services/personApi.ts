import apiClient from "@/api/client/apiClient";

import { PersonList, PersonSave, PersonDetail, PersonSearchParams } from "@/api/types/Person";

const BASE = "/person";

/* =========================
 * READ
 * ========================= */

export const getAllPersonen = async (): Promise<PersonList[]> => {
  const { data } = await apiClient.get<PersonList[]>(BASE);
  return data;
};

export const searchPersons = async (params: PersonSearchParams): Promise<PersonList[]> => {
  const { data } = await apiClient.get<PersonList[]>(`${BASE}/search`, {
    params,
  });
  return data;
};

export const getPersonById = async (id: number): Promise<PersonDetail> => {
  const { data } = await apiClient.get<PersonDetail>(`${BASE}/${id}`);
  return data;
};

/* =========================
 * CREATE
 * ========================= */

export async function createPerson(
  payload: PersonSave, // ✅
): Promise<PersonDetail> {
  const res = await apiClient.post<PersonDetail>("/person", payload);
  return res.data;
}

/* =========================
 * UPDATE
 * ========================= */

export async function updatePerson(
  id: number,
  payload: PersonSave, // ✅ RICHTIG
): Promise<PersonDetail> {
  const res = await apiClient.put<PersonDetail>(`/person/${id}`, payload);
  return res.data;
}

/* =========================
 * DELETE
 * ========================= */

export const deletePerson = async (id: number): Promise<void> => {
  await apiClient.delete(`${BASE}/${id}`);
};
