import { Box, Tabs, Tab, Alert } from "@mui/material";
import { useState } from "react";
import type { SyntheticEvent } from "react";
import { useParams, useLocation } from "react-router-dom";

import FinanzenDashboard from "@/components/finanzen/FinanzenDashboard";
import PlanungPage from "@/components/finanzen/planung/PlanungPage";
import BuchungenPage from "@/components/finanzen/buchung/BuchungenPage";
import BeitraegePage from "@/components/finanzen/beitraege/BeitraegePage";
import ReisekostenPage from "@/components/finanzen/reisekosten/ReisekostenPage";
import KuerzelPage from "@/components/finanzen/KuerzelPage";
import SimulationPage from "@/components/simulation/SimulationPage";

const Finanzen = () => {
  const { veranstaltungId } = useParams<{ veranstaltungId: string }>();
  const location = useLocation();
  const [tab, setTab] = useState(location.state?.tab ?? 0);

  if (!veranstaltungId) return null;

  const id = Number(veranstaltungId);

  if (Number.isNaN(id)) {
    return <Alert severity="error">Ungültige Veranstaltungs-ID</Alert>;
  }

  const handleChange = (_event: SyntheticEvent, value: number) => {
    setTab(value);
  };

  return (
    <Box>
      <Tabs
        value={tab}
        onChange={handleChange}
        variant="scrollable"
        scrollButtons="auto"
        allowScrollButtonsMobile
        sx={{
          mb: 2,
          minHeight: 42,

          "& .MuiTab-root": {
            minHeight: 42,
            px: 1.2,

            fontSize: {
              xs: "0.72rem",
              sm: "1.2rem",
            },

            minWidth: "auto",
          },
        }}
      >
        <Tab label="Dashboard" />
        <Tab label="Simulation" />
        <Tab label="Planung" />
        <Tab label="Abrechnung" />
        <Tab label="Fahrkosten" />
        <Tab label="Beiträge" />

        <Tab label="Kürzel" />
      </Tabs>

      {tab === 0 && <FinanzenDashboard />}
      {tab === 1 && (
        <SimulationPage
          veranstaltungId={id}
        />
      )}
      {tab === 2 && (
        <PlanungPage
          veranstaltungId={id}
          onOpenSimulation={() => setTab(5)}
        />
      )}
      {tab === 3 && <BuchungenPage veranstaltungId={id} />}
      {tab === 4 && <ReisekostenPage veranstaltungId={id} />}
      {tab === 5 && <BeitraegePage veranstaltungId={id} />}
      {tab === 6 && <KuerzelPage veranstaltungId={id} />}
    </Box>
  );
};

export default Finanzen;
