// src/vermietung/pages/VermietungStart.tsx
import { Box, Grid } from "@mui/material";
import { useNavigate } from "react-router-dom";

import { ModuleButton } from "@/core/components/common/ModuleButton";
import { moduleTypeMap } from "@/core/theme/moduleMap";

const VermietungStart = () => {
  const navigate = useNavigate();

  return (
    <Box sx={{ p: 3 }}>
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <ModuleButton
            label="Mieter"
            moduleType={moduleTypeMap.mieter}
            onClick={() => navigate("/vermietung/mieter")}
          />
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <ModuleButton
            label="Mietobjekte"
            moduleType={moduleTypeMap.mietobjekte}
            onClick={() => navigate("/vermietung/mietobjekte")}
          />
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <ModuleButton
            label="Buchung"
            moduleType={moduleTypeMap.buchung}
            onClick={() => navigate("/vermietung/buchung")}
          />
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <ModuleButton
            label="Abrechnung"
            moduleType={moduleTypeMap.abrechnung}
            onClick={() => navigate("/vermietung/abrechnung")}
          />
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <ModuleButton
            label="Verwaltung"
            moduleType={moduleTypeMap.vermietungVerwaltung}
            onClick={() => navigate("/vermietung/verwaltung")}
          />
        </Grid>
      </Grid>
    </Box>
  );
};

export default VermietungStart;
