// FoerdersatzAdminPage.tsx

import { Box, Typography } from "@mui/material";

import FoerdersatzTable from "@/components/admin/foerdersatz/FoerdersatzTable";

export default function FoerdersatzAdminPage() {

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" gutterBottom>
        Fördersätze
      </Typography>

      <FoerdersatzTable />


    </Box>
  );
}
