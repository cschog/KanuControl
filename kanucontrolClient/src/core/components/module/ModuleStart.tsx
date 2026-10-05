import { Box, Grid } from "@mui/material";
import { useEffect, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { ModuleType } from "@/core/theme/moduleColors";

import { ModuleButton } from "@/core/components/common/ModuleButton";
import { moduleTypeMap } from "@/core/theme/moduleMap";
import { hasRole, isAdmin } from "@/core/auth/useTenant";

type AppModule = {
  key: "KJFP" | "VERMIETUNG" | "FAHRTEN";
  label: string;
  role: string;
  path: string;
  moduleType: ModuleType;
};

const modules: AppModule[] = [
  {
    key: "KJFP",
    label: "KJFP",
    role: "KJFP",
    path: "/startmenue",
    moduleType: moduleTypeMap.vereine,
  },
  {
    key: "VERMIETUNG",
    label: "Vermietung",
    role: "VERMIETUNG",
    path: "/vermietung",
    moduleType: moduleTypeMap.finanzen,
  },
  {
    key: "FAHRTEN",
    label: "Fahrten",
    role: "FAHRTEN",
    path: "/fahrten",
    moduleType: moduleTypeMap.finanzen,
  },
];

const ModuleStart = () => {
  const navigate = useNavigate();

  const availableModules = useMemo(() => {
    if (isAdmin()) {
      return modules;
    }

    return modules.filter((module) => hasRole(module.role));
  }, []);

  useEffect(() => {
    if (availableModules.length === 1) {
      navigate(availableModules[0].path, { replace: true });
    }
  }, [availableModules, navigate]);

  // Noch keine Berechtigung für ein Modul
  if (availableModules.length === 0) {
    return <Box sx={{ p: 3 }}>Für diesen Benutzer ist kein KanuControl-Modul freigeschaltet.</Box>;
  }

  // Bei genau einem Modul erfolgt die Weiterleitung automatisch.
  if (availableModules.length === 1) {
    return null;
  }

  return (
    <Box sx={{ p: 3 }}>
      <Grid container spacing={2}>
        {availableModules.map((module) => (
          <Grid key={module.key} size={{ xs: 12, sm: 6, md: 4 }}>
            <ModuleButton
              label={module.label}
              moduleType={module.moduleType}
              onClick={() => navigate(module.path)}
            />
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default ModuleStart;
