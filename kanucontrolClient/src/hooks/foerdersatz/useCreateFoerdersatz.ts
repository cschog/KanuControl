// src/hooks/foerdersatz/useCreateFoerdersatz.ts

import { useMutation, useQueryClient } from "@tanstack/react-query";

import { createFoerdersatz } from "@/api/services/foerdersatzApi";

import type { FoerdersatzCreateUpdateDTO } from "@/api/types/Foerdersatz";

export function useCreateFoerdersatz() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (dto: FoerdersatzCreateUpdateDTO) => createFoerdersatz(dto),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["foerdersaetze"],
      });
    },
  });
}
