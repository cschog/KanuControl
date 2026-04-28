// src/hooks/finanzen/useFinanzenDashboard.ts

import { useQuery } from "@tanstack/react-query";

import { getFinanzenDashboard } from "@/api/services/finanzenApi";

export function useFinanzenDashboard(veranstaltungId?: number) {
  return useQuery({
    queryKey: ["finanzen-dashboard", veranstaltungId],

    queryFn: () => getFinanzenDashboard(veranstaltungId!),

    enabled: !!veranstaltungId,
  });
}
