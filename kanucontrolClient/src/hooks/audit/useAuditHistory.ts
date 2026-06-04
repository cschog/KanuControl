// src/hooks/useAuditHistory.ts

import { useQuery } from "@tanstack/react-query";
import { getAuditHistory } from "@/api/services/auditApi";

export function useAuditHistory(page: number, size: number) {
  return useQuery({
    queryKey: ["audit-history", page, size],
    queryFn: () => getAuditHistory(page, size),
  });
}
