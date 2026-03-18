import { Box, Tabs, Tab } from "@mui/material";
import { useState } from "react";
import type { SyntheticEvent } from "react";
import { useParams } from "react-router-dom";

import FinanzenDashboard from "@/components/finanzen/FinanzenDashboard";
import PlanungPage from "@/components/finanzen/PlanungPage";
import BuchungenPage from "@/components/finanzen/BuchungenPage";
import AbrechnungPage from "@/components/finanzen/AbrechnungPage";
import VergleichPage from "@/components/finanzen/VergleichPage";
import KuerzelPage from "@/components/finanzen/KuerzelPage";

const Finanzen = () => {
  const { veranstaltungId } = useParams<{ veranstaltungId: string }>();
  const [tab, setTab] = useState(0);

  if (!veranstaltungId) return null;

  const id = Number(veranstaltungId);

  const handleChange = (_event: SyntheticEvent, value: number) => {
    setTab(value);
  };

  return (
    <Box>
      <Tabs
        value={tab}
        onChange={handleChange}
        sx={{ mb: 2 }}
        variant="scrollable"
        scrollButtons="auto"
      >
        <Tab label="Dashboard" />
        <Tab label="Planung" />
        <Tab label="Buchungen" />
        <Tab label="Abrechnung" />
        <Tab label="Vergleich" />
        <Tab label="Kürzel" />
      </Tabs>

      {tab === 0 && <FinanzenDashboard />}
      {tab === 1 && <PlanungPage veranstaltungId={id} />}
      {tab === 2 && <BuchungenPage veranstaltungId={id} />}
      {tab === 3 && <AbrechnungPage veranstaltungId={id} />}
      {tab === 4 && <VergleichPage veranstaltungId={id} />}
      {tab === 5 && <KuerzelPage veranstaltungId={id} />}
    </Box>
  );
};

export default Finanzen;
