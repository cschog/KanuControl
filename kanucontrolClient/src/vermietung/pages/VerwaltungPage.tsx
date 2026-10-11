import { Box, Typography, Tabs, Tab } from "@mui/material";
import { useState } from "react";

import MietpreiseVerwaltung from "@/vermietung/components/verwaltung/MietpreiseVerwaltung";
import RabatteVerwaltung from "@/vermietung/components/verwaltung/RabatteVerwaltung";
import SortierungVerwaltung from "@/vermietung/components/verwaltung/SortierungVerwaltung";
import VerbrauchskostenVerwaltung from "@/vermietung/components/verwaltung/VerbrauchskostenVerwaltung";
import ZaehlerVerwaltung from "@/vermietung/components/verwaltung/ZaehlerVerwaltung";

const VerwaltungPage = () => {
  const [tab, setTab] = useState(0);

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" gutterBottom>
        Vermietung – Verwaltung
      </Typography>

      <Tabs
        value={tab}
        onChange={(_, value) => setTab(value)}
        sx={{ mb: 3 }}
        variant="scrollable"
        scrollButtons="auto"
      >
        <Tab label="Mietpreise" />
        <Tab label="Rabatte" />
        <Tab label="Verbrauchskosten" />
        <Tab label="Sortierung" />
        <Tab label="Zählerverwaltung" />
      </Tabs>

      {tab === 0 && <MietpreiseVerwaltung />}
      {tab === 1 && <RabatteVerwaltung />}
      {tab === 2 && <VerbrauchskostenVerwaltung />}
      {tab === 3 && <SortierungVerwaltung />}
      {tab === 4 && <ZaehlerVerwaltung />}
    </Box>
  );
};

export default VerwaltungPage;
