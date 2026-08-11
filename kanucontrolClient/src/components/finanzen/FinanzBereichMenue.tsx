import { Box, Grid, Typography } from "@mui/material";
import { useNavigate, useParams } from "react-router-dom";

import { ModuleButton } from "@/components/common/ModuleButton";
import { moduleTypeMap } from "@/theme/moduleMap";

interface FinanzModul {
  key: string;
  label: string;
  path: string;
}

interface Props {
  title: string;
  module: FinanzModul[];
}

const FinanzBereichMenue = ({ title, module }: Props) => {
  const navigate = useNavigate();
  const { veranstaltungId } = useParams<{ veranstaltungId: string }>();

  if (!veranstaltungId) {
    return null;
  }

  return (
    <Box>
      <Typography
        variant="h5"
        sx={{
          mb: 2,
          fontWeight: 700,
        }}
      >
        {title}
      </Typography>

      <Grid container spacing={2}>
        {module.map((modul) => (
          <Grid key={modul.key} size={{ xs: 12, sm: 6, md: 4 }}>
            <ModuleButton
              label={modul.label}
              moduleType={moduleTypeMap.finanzen}
              onClick={() => navigate(`/veranstaltungen/${veranstaltungId}/finanzen/${modul.path}`)}
            />
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default FinanzBereichMenue;
