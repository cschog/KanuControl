import apiClient from "@/api/client/apiClient";

import { PersonList } from "@/api/types/PersonList";
import { PersonDetail } from "@/api/types/PersonDetail";
import { PersonSearchParams } from "@/api/types/PersonSearchParams";

import { toPersonSaveDTO } from "@/api/mappers/personMapper";

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

export const createPerson = async (person: PersonDetail): Promise<PersonDetail> => {
  const dto = toPersonSaveDTO(person);
  const { data } = await apiClient.post<PersonDetail>(BASE, dto);
  return data;
};

/* =========================
 * UPDATE
 * ========================= */

export const updatePerson = async (person: PersonDetail): Promise<PersonDetail> => {
  const dto = toPersonSaveDTO(person);
  const { data } = await apiClient.put<PersonDetail>(`${BASE}/${person.id}`, dto);
  return data;
};

/* =========================
 * DELETE
 * ========================= */

export const deletePerson = async (id: number): Promise<void> => {
  await apiClient.delete(`${BASE}/${id}`);
};
