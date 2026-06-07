import { Box, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";

import ReisekostenKonfigurationTable from "@/components/admin/reisekosten/ReisekostenKonfigurationTable";
import { BottomActionBar } from "@/components/layout/BottomActionBar";

export default function ReisekostenKonfigurationAdminPage() {
  const navigate = useNavigate();

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" gutterBottom>
        Fahrkosten-Konfiguration
      </Typography>

      <ReisekostenKonfigurationTable />

      <BottomActionBar
        left={[
          {
            label: "Zurück",
            onClick: () => navigate("/admin"),
            variant: "outlined",
          },
        ]}
      />
    </Box>
  );
}
