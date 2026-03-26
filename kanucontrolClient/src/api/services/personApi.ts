import { PersonFilterState } from "@/api/types/PersonFilterState";
import apiClient from "@/api/client/apiClient";
import { PersonSave, PersonDetail, PersonList } from "@/api/types/Person";
import { PersonRef } from "@/api/types/PersonRef";

/* =========================================================
   TYPES
   ========================================================= */

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
}

   /* =========================================================
   SCROLL (Cursor-based Pagination)
   ========================================================= */

export interface ScrollResponse<T> {
  content: T[];
  total: number;
}

export async function getPersonsScroll(
  cursorName?: string,
  cursorVorname?: string,
  cursorId?: number,
  size = 25,
  filters?: PersonFilterState,
): Promise<ScrollResponse<PersonList>> {
  const res = await apiClient.get<ScrollResponse<PersonList>>("/person/scroll", {
    params: {
      cursorName,
      cursorVorname,
      cursorId,
      size,
      ...filters,
    },
  });

  return res.data;
}

export async function getPersonsPaged(
  page: number,
  size: number,
  filters?: PersonFilterState,
  sortField?: string,
  sortDirection?: "asc" | "desc",
) {
  const res = await apiClient.get("/person/search", {
    params: {
      page,
      size,
      ...filters,
      sort: sortField ? `${sortField},${sortDirection ?? "asc"}` : undefined,
    },
  });

  return res.data;
}

/* =========================================================
   SEARCH (Autocomplete)
   ========================================================= */

export async function searchPersons(params: { search?: string }): Promise<PersonRef[]> {
  const res = await apiClient.get<PersonRef[]>("/person/search", {
    params: {
      search: params.search,
    },
  });

  return res.data ?? []; // ✅ wichtig!
}

/* =========================================================
   DETAIL
   ========================================================= */

export async function getPersonById(id: number): Promise<PersonDetail> {
  const res = await apiClient.get<PersonDetail>(`/person/${id}`);
  return res.data;
}

/* =========================================================
   CREATE
   ========================================================= */

export async function createPerson(payload: PersonSave): Promise<PersonDetail> {
  const res = await apiClient.post<PersonDetail>("/person", payload);
  return res.data;
}

/* =========================================================
   UPDATE
   ========================================================= */

export async function updatePerson(id: number, payload: PersonSave): Promise<PersonDetail> {
  const res = await apiClient.put<PersonDetail>(`/person/${id}`, payload);
  return res.data;
}

/* =========================================================
   DELETE
   ========================================================= */

export async function deletePerson(id: number): Promise<void> {
  await apiClient.delete(`/person/${id}`);
}
