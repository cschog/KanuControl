// src/hooks/reisekosten/useReisekostenabrechnung.ts

import { useQuery } from "@tanstack/react-query";

import { getReisekostenabrechnung } from "@/api/services/reisekostenApi";

import { ReisekostenabrechnungDetailResponse } from "@/api/types/Reisekostenabrechnung";

export function useReisekostenabrechnung(id: number) {
  return useQuery<ReisekostenabrechnungDetailResponse>({
    queryKey: ["reisekosten", id],
    queryFn: () => getReisekostenabrechnung(id),
    enabled: !!id,
  });
}
