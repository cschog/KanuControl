// src/hooks/kik/useCreateKikZuschlag.ts

import { useMutation, useQueryClient } from "@tanstack/react-query";

import { createKikZuschlag } from "@/kjfp/api/services/kikZuschlagApi";

import type { KikCreateUpdateDTO } from "@/kjfp/types/Kik";

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
