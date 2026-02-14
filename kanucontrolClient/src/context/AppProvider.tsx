import React, { useEffect, useState } from "react";
import apiClient from "@/api/client/apiClient";
import { getActiveVeranstaltung } from "@/api/services/veranstaltungApi";
import { VeranstaltungDetail } from "@/api/types/VeranstaltungDetail";
import { AppContext } from "./AppContext";
import keycloak from "@/auth/keycloak";

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [schema, setSchema] = useState("");
  const [active, setActive] = useState<VeranstaltungDetail | null>(null);
  const [loading, setLoading] = useState(true);

  const loadContext = async () => {
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
  };

useEffect(() => {
  const waitForAuth = async () => {
    // warten bis Keycloak Token hat
    let tries = 0;
    while (!keycloak.authenticated && tries < 20) {
      await new Promise((r) => setTimeout(r, 100));
      tries++;
    }

    await loadContext();
  };

  waitForAuth();
}, []);

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
