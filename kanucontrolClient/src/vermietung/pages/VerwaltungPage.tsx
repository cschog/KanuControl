import AirbnbExportVerwaltung from "@/vermietung/components/verwaltung/AirbnbExportVerwaltung";
import SperrzeitenVerwaltung from "@/vermietung/components/verwaltung/SperrzeitenVerwaltung";
import { Box, Typography, Tabs, Tab } from "@mui/material";
import { useState } from "react";

import MietpreiseVerwaltung from "@/vermietung/components/verwaltung/MietpreiseVerwaltung";
import RabatteVerwaltung from "@/vermietung/components/verwaltung/RabatteVerwaltung";
import SortierungVerwaltung from "@/vermietung/components/verwaltung/SortierungVerwaltung";
import VerbrauchskostenVerwaltung from "@/vermietung/components/verwaltung/VerbrauchskostenVerwaltung";
import ZaehlerVerwaltung from "@/vermietung/components/verwaltung/ZaehlerVerwaltung";
import MietbereichAbhaengigkeitenVerwaltung from "@/vermietung/components/verwaltung/MietbereichAbhaengigkeitenVerwaltung";

import HomepageAnbindungVerwaltung from "@/vermietung/components/verwaltung/HomepageAnbindungVerwaltung";

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
        <Tab label="Abhängigkeiten" />
        <Tab label="Homepage-Anbindung" />
        <Tab label="Sperrzeiten" />
        <Tab label="Airbnb-Export" />
      </Tabs>

      {tab === 0 && <MietpreiseVerwaltung />}
      {tab === 1 && <RabatteVerwaltung />}
      {tab === 2 && <VerbrauchskostenVerwaltung />}
      {tab === 3 && <SortierungVerwaltung />}
      {tab === 4 && <ZaehlerVerwaltung />}
      {tab === 5 && <MietbereichAbhaengigkeitenVerwaltung />}
      {tab === 6 && <HomepageAnbindungVerwaltung />}
      {tab === 7 && <SperrzeitenVerwaltung />}
      {tab === 8 && <AirbnbExportVerwaltung />}
    </Box>
  );
};

export default VerwaltungPage;
