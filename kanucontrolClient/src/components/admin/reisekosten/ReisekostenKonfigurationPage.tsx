import { Box, Typography } from "@mui/material";

import ReisekostenKonfigurationTable from "@/components/admin/reisekosten/ReisekostenKonfigurationTable";

export default function ReisekostenKonfigurationAdminPage() {

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" gutterBottom>
        Fahrkosten-Konfiguration
      </Typography>

      <ReisekostenKonfigurationTable />

    
    </Box>
  );
}
