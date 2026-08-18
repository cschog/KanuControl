// hooks/reisekosten/useReisekostenabrechnungen.ts
import { useQuery } from "@tanstack/react-query";

import { getReisekostenabrechnungenByVeranstaltung } from "@/api/services/reisekostenApi";

export function useReisekostenabrechnungen(veranstaltungId: number) {
  return useQuery({
    queryKey: ["reisekosten", "veranstaltung", veranstaltungId],
    queryFn: () => getReisekostenabrechnungenByVeranstaltung(veranstaltungId),
  });
}
