// src/hooks/foerdersatz/useFoerdersaetze.ts

import { useQuery } from "@tanstack/react-query";

import { getFoerdersaetze } from "@/kjfp/api/services/foerdersatzApi";

export function useFoerdersaetze() {
  return useQuery({
    queryKey: ["foerdersaetze"],

    queryFn: getFoerdersaetze,
  });
}
