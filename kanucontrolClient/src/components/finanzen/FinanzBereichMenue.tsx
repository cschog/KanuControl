import { Box, Grid, Typography } from "@mui/material";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import { ModuleButton } from "@/components/common/ModuleButton";
import { moduleTypeMap } from "@/theme/moduleMap";
import { getPlanung } from "@/api/services/planungApi";

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

  const [hatPlanung, setHatPlanung] = useState(false);

  useEffect(() => {
    if (!veranstaltungId) {
      return;
    }

    // Nur prüfen, wenn dieses Menü die Planung überhaupt enthält.
    const enthaeltPlanung = module.some((modul) => modul.key === "planung");

    if (!enthaeltPlanung) {
      return;
    }

    getPlanung(Number(veranstaltungId))
      .then((planung) => {
        setHatPlanung(planung !== null);
      })
      .catch(() => {
        setHatPlanung(false);
      });
  }, [veranstaltungId, module]);

  if (!veranstaltungId) {
    return null;
  }

  const sichtbareModule = module.filter((modul) => modul.key !== "planung" || hatPlanung);

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
        {sichtbareModule.map((modul) => (
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
