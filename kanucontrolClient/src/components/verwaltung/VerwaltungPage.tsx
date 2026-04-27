import { Box, Tab, Tabs } from "@mui/material";
import { useState } from "react";

import FoerdersatzTable from "@/components/verwaltung/foerdersatz/FoerdersatzTable";
import KikZuschlagTable from "@/components/verwaltung/kik/KikZuschlagTable";

const VerwaltungPage = () => {
  const [tab, setTab] = useState(0);

  return (
    <Box>
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
        <Tab label="Fördersätze" />
        <Tab label="KiK-Zuschläge" />
      </Tabs>

      {tab === 0 && <FoerdersatzTable />}
      {tab === 1 && <KikZuschlagTable />}
    </Box>
  );
};

export default VerwaltungPage;
