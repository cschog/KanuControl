// src/pages/admin/AdminPage.tsx

import { Box, Button, Grid } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { BottomActionBar } from "@/components/layout/BottomActionBar";

export default function AdminPage() {
  const navigate = useNavigate();

  return (
    <Box>
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 4 }}>
          <Button
            fullWidth
            variant="outlined"
            sx={{ height: 80 }}
            onClick={() => navigate("/admin/postal-codes")}
          >
            PLZ-Verwaltung
          </Button>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Button
            fullWidth
            variant="outlined"
            sx={{ height: 80 }}
            onClick={() => navigate("/admin/foerdersaetze")}
          >
            Fördersätze
          </Button>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Button
            fullWidth
            variant="outlined"
            sx={{ height: 80 }}
            onClick={() => navigate("/admin/kik-zuschlaege")}
          >
            KiK-Zuschläge
          </Button>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Button
            fullWidth
            variant="outlined"
            sx={{ height: 80 }}
            onClick={() => navigate("/admin/reisekosten")}
          >
            Reisekosten
          </Button>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <Button
            fullWidth
            variant="outlined"
            sx={{ height: 80 }}
            onClick={() => navigate("/admin/audit")}
          >
            Audit
          </Button>
        </Grid>
      </Grid>

      <BottomActionBar
        left={[
          {
            label: "Zurück",
            onClick: () => navigate("/startmenue"),
            variant: "outlined",
          },
        ]}
      />
    </Box>
  );
}
