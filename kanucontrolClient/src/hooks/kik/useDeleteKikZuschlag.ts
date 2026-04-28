// src/hooks/kik/useDeleteKikZuschlag.ts

import { useMutation, useQueryClient } from "@tanstack/react-query";

import { deleteKikZuschlag } from "@/api/services/kikZuschlagApi";

export function useDeleteKikZuschlag() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => deleteKikZuschlag(id),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["kikZuschlag"],
      });
    },
  });
}
