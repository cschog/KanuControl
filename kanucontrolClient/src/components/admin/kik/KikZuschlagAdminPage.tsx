// KikZuschlagAdminPage.tsx

import { Box, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";

import KikZuschlagTable from "@/components/admin/kik/KikZuschlagTable";
import { BottomActionBar } from "@/components/layout/BottomActionBar";

export default function KikZuschlagAdminPage() {
  const navigate = useNavigate();

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" gutterBottom>
        KiK-Zuschläge
      </Typography>

      <KikZuschlagTable />

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
