// hooks/reisekosten/useReisekostenabrechnungen.ts
import { useQuery } from "@tanstack/react-query";

import { getReisekostenabrechnungenByVeranstaltung } from "@/kjfp/api/services/reisekostenApi";

export function useReisekostenabrechnungen(veranstaltungId: number) {
  return useQuery({
    queryKey: ["reisekosten", "veranstaltung", veranstaltungId],
    queryFn: () => getReisekostenabrechnungenByVeranstaltung(veranstaltungId),
  });
}
