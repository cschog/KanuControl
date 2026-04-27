// src/hooks/foerdersatz/useDeleteFoerdersatz.ts

import { useMutation, useQueryClient } from "@tanstack/react-query";

import { deleteFoerdersatz } from "@/api/client/foerdersatzApi";

export function useDeleteFoerdersatz() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => deleteFoerdersatz(id),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["foerdersaetze"],
      });
    },
  });
}
