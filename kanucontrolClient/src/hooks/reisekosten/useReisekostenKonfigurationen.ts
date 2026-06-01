import { useQuery } from "@tanstack/react-query";

import { getReisekostenKonfigurationen } from "@/api/services/reisekostenKonfigurationApi";

export function useReisekostenKonfigurationen() {
  return useQuery({
    queryKey: ["reisekosten-konfigurationen"],
    queryFn: getReisekostenKonfigurationen,
  });
}
