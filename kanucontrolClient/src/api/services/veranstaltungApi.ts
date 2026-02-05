// src/api/services/veranstaltungApi.ts
import apiClient from "@/api/client/apiClient";
import { Page } from "@/api/types/Page";
import { VeranstaltungList } from "@/api/types/VeranstaltungList";

export function getVeranstaltungenPage(page: number, size: number) {
  return apiClient
    .get<Page<VeranstaltungList>>("/veranstaltung", {
      params: {
        page,
        size,
        sort: "beginnDatum,desc",
      },
    })
    .then((r) => r.data);
}
