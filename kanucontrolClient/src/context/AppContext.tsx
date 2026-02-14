import { createContext, useContext } from "react";
import { VeranstaltungDetail } from "@/api/types/VeranstaltungDetail";

export interface AppContextType {
  schema: string;
  active: VeranstaltungDetail | null;
  loading: boolean;
  reload: () => Promise<void>;
}

export const AppContext = createContext<AppContextType | undefined>(undefined);

export function useAppContext() {
  const ctx = useContext(AppContext);
  if (!ctx) {
    throw new Error("useAppContext must be used inside AppProvider");
  }
  return ctx;
}
