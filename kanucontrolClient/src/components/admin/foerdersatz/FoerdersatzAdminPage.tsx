// FoerdersatzAdminPage.tsx

import { Box, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";

import FoerdersatzTable from "@/components/admin/foerdersatz/FoerdersatzTable";
import { BottomActionBar } from "@/components/layout/BottomActionBar";

export default function FoerdersatzAdminPage() {
  const navigate = useNavigate();

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" gutterBottom>
        Fördersätze
      </Typography>

      <FoerdersatzTable />

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
