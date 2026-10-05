// src/hooks/useActiveSessions.ts

import { useQuery } from "@tanstack/react-query";
import { getActiveSessions } from "@/kjfp/api/services/auditApi";

export function useActiveSessions() {
  return useQuery({
    queryKey: ["active-sessions"],
    queryFn: getActiveSessions,
    refetchInterval: 30000,
  });
}
