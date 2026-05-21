import { Box, Tabs, Tab, Alert } from "@mui/material";
import { useState } from "react";
import type { SyntheticEvent } from "react";
import { useParams } from "react-router-dom";

import FinanzenDashboard from "@/components/finanzen/FinanzenDashboard";
import PlanungPage from "@/components/finanzen/PlanungPage";
import BuchungenPage from "@/components/finanzen/BuchungenPage";
import BeitraegePage from "@/components/finanzen/BeitraegePage";
import AbrechnungPage from "@/components/finanzen/AbrechnungPage";
import KuerzelPage from "@/components/finanzen/KuerzelPage";

const Finanzen = () => {
  const { veranstaltungId } = useParams<{ veranstaltungId: string }>();
  const [tab, setTab] = useState(0);

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
        <Tab label="Planung" />
        <Tab label="Buchungen" />
        <Tab label="Beiträge" />
        <Tab label="Abrechnung" />
        <Tab label="Kürzel" />
      </Tabs>

      {tab === 0 && <FinanzenDashboard />}
      {tab === 1 && <PlanungPage veranstaltungId={id} />}
      {tab === 2 && <BuchungenPage veranstaltungId={id} />}
      {tab === 3 && <BeitraegePage veranstaltungId={id} />}
      {tab === 4 && <AbrechnungPage veranstaltungId={id} />}
      {tab === 5 && <KuerzelPage veranstaltungId={id} />}
    </Box>
  );
};

export default Finanzen;
