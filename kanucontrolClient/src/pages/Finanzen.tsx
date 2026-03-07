import { Box, Tabs, Tab } from "@mui/material";
import { useState } from "react";
import type { SyntheticEvent } from "react";

import FinanzenDashboard from "@/components/finanzen/FinanzenDashboard";
import PlanungPage from "@/components/finanzen/PlanungPage";
import KostenPage from "@/components/finanzen/KostenPage";
import FinanzMatrix from "@/components/finanzen/FinanzMatrix";

const Finanzen = () => {
  const [tab, setTab] = useState(0);

  const handleChange = (_event: SyntheticEvent, value: number) => {
    setTab(value);
  };

  return (
    <Box>
      <Tabs value={tab} onChange={handleChange} sx={{ mb: 2 }}>
        <Tab label="Dashboard" />
        <Tab label="Planung" />
        <Tab label="Kosten" />
        <Tab label="Überblick" />
      </Tabs>

      {tab === 0 && <FinanzenDashboard />}
      {tab === 1 && <PlanungPage />}
      {tab === 2 && <KostenPage />}
      {tab === 3 && <FinanzMatrix />}
    </Box>
  );
};

export default Finanzen;