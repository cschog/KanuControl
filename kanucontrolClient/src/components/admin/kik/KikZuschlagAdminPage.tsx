// KikZuschlagAdminPage.tsx

import { Box, Typography } from "@mui/material";

import KikZuschlagTable from "@/components/admin/kik/KikZuschlagTable";

export default function KikZuschlagAdminPage() {

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" gutterBottom>
        KiK-Zuschläge
      </Typography>

      <KikZuschlagTable />

  
    </Box>
  );
}
