import { Box } from "@mui/material";
import Grid from "@mui/material/Grid";
import { useNavigate } from "react-router-dom";
import { ModuleButton } from "@/core/components/common/ModuleButton";
import { moduleTypeMap } from "@/core/theme/moduleMap";

const buttons = [
  { key: "personen", label: "Personen", path: "/personen" },
  { key: "aktionen", label: "Aktionen", path: "/veranstaltungen" },
  { key: "teilnehmer", label: "Teilnehmer", path: "/teilnehmer" },
  { key: "kanus", label: "Kanus", path: "/fahrten/kanus" },
  { key: "gewaesser", label: "Flüsse", path: "/fahrten/gewaesser" },
  { key: "fahrten", label: "Fahrten", path: "/fahrten/liste" },
] as const;

export default function FahrtenStartPage() {
  const navigate = useNavigate();
  return <Box sx={{ p: { xs: 2, md: 4 } }}>
    <Grid container spacing={3}>
      {buttons.map((button) => <Grid key={button.key} size={{ xs: 12, sm: 6, md: 4 }}>
        <ModuleButton label={button.label} moduleType={moduleTypeMap[button.key]} onClick={() => navigate(button.path)} />
      </Grid>)}
    </Grid>
  </Box>;
}
