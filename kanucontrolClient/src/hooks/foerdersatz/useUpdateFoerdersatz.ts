// src/hooks/foerdersatz/useUpdateFoerdersatz.ts

import { useMutation, useQueryClient } from "@tanstack/react-query";

import { updateFoerdersatz } from "@/api/services/foerdersatzApi";

import type { FoerdersatzCreateUpdateDTO } from "@/api/types/Foerdersatz";

interface Params {
  id: number;
  dto: FoerdersatzCreateUpdateDTO;
}

export function useUpdateFoerdersatz() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, dto }: Params) => updateFoerdersatz(id, dto),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["foerdersaetze"],
      });
    },
  });
}
