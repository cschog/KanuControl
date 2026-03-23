import apiClient from "@/api/client/apiClient";
import { PersonList, PersonSave, PersonDetail } from "@/api/types/Person";
import { PersonFilterState } from "@/components/person/Personen";

const BASE = "/person";



/* =========================
 * UNPAGED (Scroll ohne Paging)
 * ========================= */
export const getAllPersons = async (
  filters?: PersonFilterState,
  sortField?: string,
  sortDirection?: "asc" | "desc",
): Promise<PersonList[]> => {
  const { data } = await apiClient.get<PersonList[]>("/person/all", {
    params: {
      ...(filters ?? {}),
      sort: sortField ? `${sortField},${sortDirection ?? "asc"}` : undefined,
    },
  });

  return data;
};

/* =========================
 * SEARCH (Autocomplete)
 * ========================= */
type SearchParams = {
  search?: string;
};

export const searchPersons = async (params: SearchParams): Promise<PersonList[]> => {
  const { search } = params;

  const { data } = await apiClient.get<PersonList[]>(`${BASE}/search`, {
    params: {
      search,
    },
  });

  return data;
};

/* =========================
 * DETAIL
 * ========================= */
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
