// src/vermietung/context/VermietungProvider.tsx
import React, { useCallback, useEffect, useState } from "react";

import { getMietobjekte } from "@/vermietung/api/mietobjektApi";
import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";

import { VermietungContext } from "./VermietungContext";

interface Props {
  children: React.ReactNode;
}

export const VermietungProvider: React.FC<Props> = ({ children }) => {
  const [mietobjekt, setMietobjekt] = useState<Mietobjekt | null>(null);

  const [loading, setLoading] = useState(true);

  const reload = useCallback(async () => {
    try {
      setLoading(true);

      const mietobjekte = await getMietobjekte();

      const aktiv = mietobjekte.find((objekt) => objekt.aktiv);

      setMietobjekt(aktiv ?? null);
    } catch (error) {
      console.error("Fehler beim Laden des aktiven Mietobjekts:", error);
      setMietobjekt(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void reload();
  }, [reload]);

  return (
    <VermietungContext.Provider
      value={{
        mietobjekt,
        loading,
        reload,
      }}
    >
      {children}
    </VermietungContext.Provider>
  );
};
