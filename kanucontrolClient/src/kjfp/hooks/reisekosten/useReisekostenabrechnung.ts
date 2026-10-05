// src/hooks/reisekosten/useReisekostenabrechnung.ts
import { useQuery } from "@tanstack/react-query";

import { getReisekostenabrechnung } from "@/kjfp/api/services/reisekostenApi";

import { ReisekostenabrechnungDetailResponse } from "@/kjfp/types/Reisekostenabrechnung";

export function useReisekostenabrechnung(id: number) {
  return useQuery<ReisekostenabrechnungDetailResponse>({
    queryKey: ["reisekosten", "detail", id],
    queryFn: () => getReisekostenabrechnung(id),
    enabled: !!id,
  });
}
