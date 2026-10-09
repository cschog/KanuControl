// src/vermietung/context/VermietungContext.tsx

import { createContext, useContext } from "react";

import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";

export interface VermietungContextType {
  mietobjekt: Mietobjekt | null;
  loading: boolean;
  reload: () => Promise<void>;
}

export const VermietungContext = createContext<VermietungContextType | undefined>(undefined);

export function useVermietungContext() {
  const ctx = useContext(VermietungContext);

  if (!ctx) {
    throw new Error("useVermietungContext must be used inside VermietungProvider");
  }

  return ctx;
}
