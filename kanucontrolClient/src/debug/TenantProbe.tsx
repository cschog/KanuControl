import { useEffect } from "react";
import apiClient from "@/api/client/apiClient";
import axios from "axios";

export default function TenantProbe() {
  useEffect(() => {
    const load = async () => {
      try {
        const { data } = await apiClient.get("/tenant");
        console.log("✅ Aktiver Tenant:", data);
      } catch (err: unknown) {
        if (axios.isAxiosError(err)) {
          console.error(
            "❌ Tenant-Check fehlgeschlagen",
            err.response?.status,
            err.response?.data
          );
        } else {
          console.error(
            "❌ Tenant-Check fehlgeschlagen (kein AxiosError)",
            err
          );
        }
      }
    };

    load();
  }, []);

  return (
    <div>
      <h1>Tenant Probe</h1>
      <p>Siehe Browser-Konsole</p>
    </div>
  );
}
