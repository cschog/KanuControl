import apiClient from "@/api/client/apiClient";
import { Person } from "@/api/types/Person";
import { PersonSearchParams } from "@/api/types/PersonSearchParams";
import { normalizePersonPayload } from "@/api/normalize/normalizePerson";

const BASE = "/person";

/* =========================
 * READ
 * ========================= */

export const getAllPersonen = async (): Promise<Person[]> => {
  const { data } = await apiClient.get<Person[]>(BASE);
  return data;
};

export const searchPersons = async (params: PersonSearchParams): Promise<Person[]> => {
  const { data } = await apiClient.get<Person[]>(`${BASE}/search`, { params });
  return data;
};

/* =========================
 * CREATE
 * ========================= */

export const createPerson = async (person: Person): Promise<Person> => {
  const payload = normalizePersonPayload(person, "create");

  const { data } = await apiClient.post<Person>(BASE, payload);
  return data;
};

/* =========================
 * UPDATE
 * ========================= */

export const updatePerson = async (person: Person): Promise<Person> => {
  if (!person.id) {
    throw new Error("updatePerson: Person ID missing");
  }

  const payload = normalizePersonPayload(person, "update");
  console.log("PUT /person payload", payload);

  const { data } = await apiClient.put<Person>(`${BASE}/${person.id}`, payload);

  return data;
};

/* =========================
 * DELETE
 * ========================= */

export const deletePerson = async (id: number): Promise<void> => {
  await apiClient.delete(`${BASE}/${id}`);
};
