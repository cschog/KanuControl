// src/hooks/kik/useKikZuschlag.ts

import { useQuery } from "@tanstack/react-query";

import { getKikZuschlag } from "@/api/services/kikZuschlagApi";

export function useKikZuschlag() {
  return useQuery({
    queryKey: ["kikZuschlag"],

    queryFn: getKikZuschlag,
  });
}
