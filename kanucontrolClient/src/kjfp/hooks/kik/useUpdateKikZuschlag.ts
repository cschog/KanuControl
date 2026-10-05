// src/hooks/foerdersatz/useUpdateFoerdersatz.ts

import { useMutation, useQueryClient } from "@tanstack/react-query";

import { updateKikZuschlag } from "@/kjfp/api/services/kikZuschlagApi";

import type { KikCreateUpdateDTO } from "@/kjfp/types/Kik";

interface Params {
  id: number;
  dto: KikCreateUpdateDTO;
}

export function useUpdateKikUpdate() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, dto }: Params) => updateKikZuschlag(id, dto),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["kikZuschlag"],
      });
    },
  });
}
