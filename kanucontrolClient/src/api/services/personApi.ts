import apiClient from "@/api/client/apiClient";
import { PersonList, PersonSave, PersonDetail, PersonSearchParams } from "@/api/types/Person";
import { Page } from "@/api/types/Page";
import { PersonFilterState } from "@/components/person/Personen";

const BASE = "/person";

/* =========================
 * READ (PAGINATED)
 * ========================= */

/**
 * 🔑 Standard-Liste (paginiert)
 */
export const getPersonenPage = async (page = 0, size = 20, filters?: PersonFilterState) => {
  const { data } = await apiClient.get("/person/search", {
    params: {
      page,
      size,
      ...(filters ?? {}),
    },
  });

  return data;
};

/**
 * 🔍 Suche (paginiert)
 */
export const searchPersonsPage = async (
  params: PersonSearchParams & { page?: number; size?: number },
): Promise<Page<PersonList>> => {
  const { data } = await apiClient.get<Page<PersonList>>(`${BASE}/search`, {
    params,
  });
  return data;
};

/**
 * 🔎 Detail
 */
export const getPersonById = async (id: number): Promise<PersonDetail> => {
  const { data } = await apiClient.get<PersonDetail>(`${BASE}/${id}`);
  return data;
};

/* =========================
 * CREATE
 * ========================= */

export const createPerson = async (payload: PersonSave): Promise<PersonDetail> => {
  const { data } = await apiClient.post<PersonDetail>(BASE, payload);
  return data;
};

/* =========================
 * UPDATE
 * ========================= */

export const updatePerson = async (id: number, payload: PersonSave): Promise<PersonDetail> => {
  const { data } = await apiClient.put<PersonDetail>(`${BASE}/${id}`, payload);
  return data;
};

/* =========================
 * DELETE
 * ========================= */

export const deletePerson = async (id: number): Promise<void> => {
  await apiClient.delete(`${BASE}/${id}`);
};
