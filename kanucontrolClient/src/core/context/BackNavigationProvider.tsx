// src/core/context/BackNavigationProvider.tsx
import { useCallback, useState } from "react";
import type { ReactNode } from "react";
import { BackNavigationContext } from "./BackNavigationContext";

interface Props {
  children: ReactNode;
}

export function BackNavigationProvider({ children }: Props) {
  const [backHandler, setBackHandler] = useState<(() => void) | null>(null);

  const registerBackHandler = useCallback((handler: (() => void) | null) => {
    setBackHandler(() => handler);
  }, []);

  const handleBack = useCallback(
    (fallback: () => void) => {
      if (backHandler) {
        backHandler();
      } else {
        fallback();
      }
    },
    [backHandler],
  );

  return (
    <BackNavigationContext.Provider value={{ registerBackHandler, handleBack }}>
      {children}
    </BackNavigationContext.Provider>
  );
}
