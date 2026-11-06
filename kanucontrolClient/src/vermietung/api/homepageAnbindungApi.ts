import apiClient from "@/core/api/client/apiClient";
import type { HomepageAnbindung } from "@/vermietung/types/HomepageAnbindung";

export async function sendHausmeisterTest(id: number): Promise<void> {
  await apiClient.post(`/vermietung/objekte/${id}/homepage-anbindung/hausmeister-test`);
}

const url = (id: number) => `/vermietung/objekte/${id}/homepage-anbindung`;

export async function getHomepageAnbindung(id: number): Promise<HomepageAnbindung> {
  return (await apiClient.get<HomepageAnbindung>(url(id))).data;
}

export async function saveHomepageAnbindung(id: number, data: HomepageAnbindung): Promise<HomepageAnbindung> {
  return (await apiClient.put<HomepageAnbindung>(url(id), data)).data;
}
