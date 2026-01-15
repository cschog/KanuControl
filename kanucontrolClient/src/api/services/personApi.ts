import apiClient from "@/api/client/apiClient";
import { Person } from "@/api/types/Person";
import { PersonSearchParams } from "@/api/types/PersonSearchParams";

const BASE = "/person";

export const getAllPersonen = async (): Promise<Person[]> => {
  const { data } = await apiClient.get<Person[]>(BASE);
  return data;
};

export const searchPersons = async (
  params: PersonSearchParams
): Promise<Person[]> => {
  const { data } = await apiClient.get<Person[]>(`${BASE}/search`, { params });
  return data;
};

export const createPerson = async (person: Person): Promise<Person> => {
  const { data } = await apiClient.post<Person>(BASE, person);
  return data;
};

export const updatePerson = async (person: Person): Promise<Person> => {
  if (!person.id) throw new Error("Person ID missing");
  const { data } = await apiClient.put<Person>(`${BASE}/${person.id}`, person);
  return data;
};

export const deletePerson = async (id: number): Promise<void> => {
  await apiClient.delete(`${BASE}/${id}`);
};