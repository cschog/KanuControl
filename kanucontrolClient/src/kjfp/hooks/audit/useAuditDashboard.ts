import { useQuery } from "@tanstack/react-query";
import { getAuditDashboard } from "@/kjfp/api/services/auditApi";

export function useAuditDashboard() {
  return useQuery({
    queryKey: ["audit-dashboard"],
    queryFn: getAuditDashboard,
    refetchInterval: 30000,
  });
}
