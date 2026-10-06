import { Box, Grid } from "@mui/material";
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

import { ModuleButton } from "@/core/components/common/ModuleButton";
import { moduleTypeMap } from "@/core/theme/moduleMap";
import { useAvailableModules } from "@/core/auth/useModules";

const ModuleStart = () => {
  const navigate = useNavigate();
  const { availableModules } = useAvailableModules();

  useEffect(() => {
    if (availableModules.length === 1) {
      navigate(availableModules[0].path, { replace: true });
    }
  }, [availableModules, navigate]);

  if (availableModules.length === 0) {
    return <Box sx={{ p: 3 }}>Für diesen Benutzer ist kein KanuControl-Modul freigeschaltet.</Box>;
  }

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
              moduleType={moduleTypeMap[module.key]}
              onClick={() => navigate(module.path)}
            />
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default ModuleStart;
