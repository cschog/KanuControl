import { useEffect } from "react";
import apiClient from "@/api/client/apiClient";
import axios from "axios";

export default function TenantProbe() {
  useEffect(() => {
    const load = async () => {
      try {
        const { data } = await apiClient.get("/admin-test");

        console.log("✅ ADMIN-TEST erfolgreich:", data);
      } catch (err: unknown) {
        if (axios.isAxiosError(err)) {
          console.error(
            "❌ ADMIN-TEST fehlgeschlagen",
            "Status:",
            err.response?.status,
            "Antwort:",
            err.response?.data,
          );
        } else {
          console.error("❌ ADMIN-TEST fehlgeschlagen (kein AxiosError)", err);
        }
      }
    };

    load();
  }, []);

  return (
    <div>
      <h1>Admin Security Probe</h1>
      <p>Ergebnis in der Browser-Konsole.</p>
    </div>
  );
}
