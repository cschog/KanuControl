// src/hooks/kik/useCreateKikZuschlag.ts

import { useMutation, useQueryClient } from "@tanstack/react-query";

import { createKikZuschlag } from "@/api/services/kikZuschlagApi";

import type { KikCreateUpdateDTO } from "@/api/types/Kik";

export function useCreateKikZuschlag() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (dto: KikCreateUpdateDTO) => createKikZuschlag(dto),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["kikZuschlag"],
      });
    },
  });
}
