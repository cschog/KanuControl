import { useMemo } from "react";
import { hasRole, isAdmin } from "@/core/auth/useTenant";

export type AppModuleKey = "KJFP" | "VERMIETUNG" | "FAHRTEN" | "admin";

export type AvailableModule = {
  key: AppModuleKey;
  label: string;
  role?: string;
  path: string;
};

export const useAvailableModules = () => {
  return useMemo(() => {
    const admin = isAdmin();

    const modules: AvailableModule[] = [
      {
        key: "KJFP",
        label: "KJFP",
        role: "KJFP",
        path: "/startmenue",
      },
      {
        key: "VERMIETUNG",
        label: "Vermietung",
        role: "VERMIETUNG",
        path: "/vermietung",
      },
      {
        key: "FAHRTEN",
        label: "Fahrten",
        role: "FAHRTEN",
        path: "/fahrten",
      },
      {
        key: "admin",
        label: "Administration",
        path: "/admin",
      },
    ];

    const availableModules = admin
      ? modules
      : modules.filter((module) => module.role && hasRole(module.role));

    const onlyKjfp = availableModules.length === 1 && availableModules[0].key === "KJFP";

    return {
      availableModules,
      onlyKjfp,
      isAdmin: admin,
    };
  }, []);
};
