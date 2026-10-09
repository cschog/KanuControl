import { Box, Typography, Tabs, Tab } from "@mui/material";
import { useState } from "react";

import MietpreiseVerwaltung from "@/vermietung/components/verwaltung/MietpreiseVerwaltung";

const VerwaltungPage = () => {
  const [tab, setTab] = useState(0);

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" gutterBottom>
        Vermietung – Verwaltung
      </Typography>

      <Tabs value={tab} onChange={(_, value) => setTab(value)} sx={{ mb: 3 }}>
        <Tab label="Mietpreise" />
        <Tab label="Rabatte" disabled />
        <Tab label="Verbrauchskosten" disabled />
      </Tabs>

      {tab === 0 && <MietpreiseVerwaltung />}
    </Box>
  );
};

export default VerwaltungPage;
