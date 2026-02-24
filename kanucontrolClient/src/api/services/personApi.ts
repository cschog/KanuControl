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
export const getPersonenPage = async (
  page = 0,
  size = 20,
  filters?: PersonFilterState,
  sortField?: string,
  sortDirection?: "asc" | "desc",
) => {
  const { data } = await apiClient.get("/person/search", {
    params: {
      page,
      size,
      ...(filters ?? {}),
      sort: sortField ? `${sortField},${sortDirection ?? "asc"}` : undefined,
    },
  });

  return data;
};

/**
 * 🔍 Suche (paginiert)
 */
type SearchParams = PersonSearchParams & {
  page?: number;
  size?: number;
  search?: string; // 👈 kommt vom Autocomplete
};

export const searchPersonsPage = async (params: SearchParams): Promise<Page<PersonList>> => {
  const { search, name, ...rest } = params;

  const { data } = await apiClient.get<Page<PersonList>>(`${BASE}/search`, {
    params: {
      ...rest,
      name: search ?? name, // 🔑 Mapping search → name
    },
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
