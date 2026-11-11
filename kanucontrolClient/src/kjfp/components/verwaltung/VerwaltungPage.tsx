import { Box, Tab, Tabs } from "@mui/material";
import { useState, Suspense, lazy } from "react";

/* =========================================================
   LAZY IMPORTS
   ========================================================= */

const BeitragsStrukturen = lazy(
  () => import("@/kjfp/components/verwaltung/beitraege/BeitragsstrukturPage"),
);

const Unterkunftsarten = lazy(
  () => import("@/kjfp/components/verwaltung/unterkunft/UnterkunftsartPage"),
);

const Verpflegungsmodelle = lazy(
  () => import("@/kjfp/components/verwaltung/verpflegung/VerpflegungsmodellPage"),
);
const Foerdersaetze = lazy(() => import("@/core/components/admin/foerdersatz/FoerdersatzAdminPage"));
const KikZuschlaege = lazy(() => import("@/core/components/admin/kik/KikZuschlagAdminPage"));
const Fahrkosten = lazy(() => import("@/core/components/admin/reisekosten/ReisekostenKonfigurationPage"));
/* =========================================================
   COMPONENT
   ========================================================= */

const VerwaltungPage = () => {
  const [tab, setTab] = useState(() => Number(localStorage.getItem("verwaltungTab")) || 0);

  /* =========================================================
     TAB CHANGE
     ========================================================= */

  const handleChange = (_event: React.SyntheticEvent, value: number) => {
    setTab(value);

    localStorage.setItem("verwaltungTab", String(value));
  };

  /* =========================================================
     UI
     ========================================================= */

  return (
    <Box>
      {/* =====================================================
          TABS
          ===================================================== */}

      <Tabs
        value={tab}
        onChange={handleChange}
        variant="scrollable"
        scrollButtons="auto"
        allowScrollButtonsMobile
        sx={{
          mb: 2,

          minHeight: {
            xs: 42,
            md: 48,
          },

          "& .MuiTab-root": {
            minHeight: {
              xs: 42,
              md: 48,
            },

            fontSize: {
              xs: "0.8rem",
              md: "0.95rem",
            },

            px: {
              xs: 1.5,
              md: 2,
            },

            textTransform: "none",

            fontWeight: 600,
          },
        }}
      >
        <Tab label="Beitragsstrukturen" />
        <Tab label="Unterkunftsarten" />
        <Tab label="Verpflegungsmodelle" />
        <Tab label="Fördersätze" />
        <Tab label="KIK-Zuschläge" />
        <Tab label="Fahrkosten-Konfiguration" />
      </Tabs>

      {/* =====================================================
          CONTENT
          ===================================================== */}

      <Suspense fallback={<div>Loading...</div>}>
        <Box
          sx={{
            overflowX: "hidden",
          }}
        >
          {tab === 0 && <BeitragsStrukturen />}
          {tab === 1 && <Unterkunftsarten />}
          {tab === 2 && <Verpflegungsmodelle />}
          {tab === 3 && <Foerdersaetze />}
          {tab === 4 && <KikZuschlaege />}
          {tab === 5 && <Fahrkosten />}
        </Box>
      </Suspense>
    </Box>
  );
};

export default VerwaltungPage;
