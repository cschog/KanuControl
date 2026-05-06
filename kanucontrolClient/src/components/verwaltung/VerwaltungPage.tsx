import { Box, Tab, Tabs } from "@mui/material";
import { useState, Suspense, lazy } from "react";

// ✅ Lazy imports außerhalb der Komponente
const FoerdersatzTable = lazy(() => import("@/components/verwaltung/foerdersatz/FoerdersatzTable"));

const KikZuschlagTable = lazy(() => import("@/components/verwaltung/kik/KikZuschlagTable"));

const BeitragsStrukturen = lazy(
  () => import("@/components/verwaltung/beitraege/BeitragsstrukturTable"),
);

const VerwaltungPage = () => {
  const [tab, setTab] = useState(() => Number(localStorage.getItem("verwaltungTab")) || 0);

const handleChange = (_event: React.SyntheticEvent, value: number) => {
  setTab(value);
  localStorage.setItem("verwaltungTab", String(value));
};

  return (
    <Box>
      {/* ✅ nur EIN Tabs */}
      <Tabs value={tab} onChange={handleChange} sx={{ mb: 2 }}>
        <Tab label="Fördersätze" />
        <Tab label="KiK-Zuschläge" />
        <Tab label="Beitragsstrukturen" />
      </Tabs>

      <Suspense fallback={<div>Loading...</div>}>
        {tab === 0 && <FoerdersatzTable />}
        {tab === 1 && <KikZuschlagTable />}
        {tab === 2 && <BeitragsStrukturen />}
      </Suspense>
    </Box>
  );
};

export default VerwaltungPage;
