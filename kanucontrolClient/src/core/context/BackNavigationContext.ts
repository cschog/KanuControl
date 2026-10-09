// src/core/context/BackNavigationContext.ts
import { createContext, useContext } from "react";

export interface BackNavigationContextType {
  registerBackHandler: (handler: (() => void) | null) => void;
  handleBack: (fallback: () => void) => void;
}

export const BackNavigationContext = createContext<BackNavigationContextType | null>(null);

export function useBackNavigation() {
  const context = useContext(BackNavigationContext);

  if (!context) {
    throw new Error(
      "useBackNavigation muss innerhalb eines BackNavigationProvider verwendet werden.",
    );
  }

  return context;
}
