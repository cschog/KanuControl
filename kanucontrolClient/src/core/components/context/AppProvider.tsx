import React, { useCallback, useEffect, useState } from "react";
import apiClient from "@/core/api/client/apiClient";
import { getActiveVeranstaltung } from "@/kjfp/api/services/veranstaltungApi";
import type { VeranstaltungDetail } from "@/kjfp/types/veranstaltung/VeranstaltungDetail";
import { AppContext } from "./AppContext";
import keycloak from "@/core/auth/keycloak";

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [schema, setSchema] = useState("");
  const [active, setActive] = useState<VeranstaltungDetail | null>(null);
  const [loading, setLoading] = useState(true);

  const loadContext = useCallback(async () => {
    setLoading(true);

    try {
      const schemaRes = await apiClient.get<string>("/active-schema");
      setSchema(schemaRes.data);

      const v = await getActiveVeranstaltung();
      setActive(v);
    } catch {
      setActive(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const waitForAuth = async () => {
      let tries = 0;

      while (!keycloak.authenticated && tries < 20) {
        await new Promise((r) => setTimeout(r, 100));
        tries++;
      }

      await loadContext();
    };

    void waitForAuth();
  }, [loadContext]);

  useEffect(() => {
    const interval = setInterval(() => {
      void loadContext();
    }, 60000);

    return () => clearInterval(interval);
  }, [loadContext]);

  return (
    <AppContext.Provider
      value={{
        schema,
        active,
        loading,
        reload: loadContext,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};
